package xyz.yaungyue.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import xyz.yaungyue.secondhand.listener.ImportProgressListener;
import xyz.yaungyue.secondhand.mapper.CityMapper;
import xyz.yaungyue.secondhand.mapper.DistrictMapper;
import xyz.yaungyue.secondhand.mapper.ProvinceMapper;
import xyz.yaungyue.secondhand.model.dto.CityDataDTO;
import xyz.yaungyue.secondhand.model.dto.ImportResult;
import xyz.yaungyue.secondhand.model.dto.ProvinceDataDTO;
import xyz.yaungyue.secondhand.model.entity.City;
import xyz.yaungyue.secondhand.model.entity.District;
import xyz.yaungyue.secondhand.model.entity.Province;
import xyz.yaungyue.secondhand.service.RegionImportService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 行政区划数据导入服务实现
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Slf4j
@Service
public class RegionImportServiceImpl implements RegionImportService {

    @Autowired
    private ProvinceMapper provinceMapper;

    @Autowired
    private CityMapper cityMapper;

    @Autowired
    private DistrictMapper districtMapper;

    @Value("${region.import.default-country-id:1}")
    private Long defaultCountryId;

    @Value("${region.import.batch-size:500}")
    private int batchSize;

    @Value("${region.import.check-duplicate:true}")
    private boolean checkDuplicate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ImportResult importRegions(String filePath) {
        return importRegions(filePath, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importRegions(String filePath, ImportProgressListener listener) {
        log.info("开始导入行政区划数据，文件路径: {}", filePath);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        ImportResult result = new ImportResult();

        try {
            // 1. 加载和解析JSON数据
            Map<String, Map<String, List<String>>> dataMap = loadRegionData(filePath);
            if (dataMap == null || dataMap.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("行政区划数据为空");
                return result;
            }
            int totalProvinces = dataMap.size();
            log.info("共发现 {} 个省份的数据", totalProvinces);

            if (listener != null) {
                listener.onImportStart(totalProvinces);
            }

            // 2. 导入数据
            int provinceIndex = 0;
            for (Map.Entry<String, Map<String, List<String>>> provinceEntry : dataMap.entrySet()) {
                provinceIndex++;
                String provinceName = provinceEntry.getKey();
                Map<String, List<String>> citiesMap = provinceEntry.getValue();

                log.info("正在处理第 {}/{} 个省份: {}", provinceIndex, totalProvinces, provinceName);

                if (listener != null) {
                    listener.onProvinceProgress(provinceIndex, provinceName, totalProvinces);
                }

                // 处理省份
                Province province = processProvince(provinceName, result);
                if (province == null) {
                    log.warn("省份处理失败: {}", provinceName);
                    continue;
                }

                // 处理城市和区县
                processCitiesAndDistricts(province, citiesMap, result, listener, provinceName);
            }

            // 3. 设置结果
            result.calculateTotalCount();
            result.setSuccess(true);
            result.setMessage(String.format("导入完成: 省份%d个, 城市%d个, 区县%d个",
                    result.getProvinceCount(), result.getCityCount(), result.getDistrictCount()));

            log.info("行政区划数据导入成功: {}", result);

        } catch (Exception e) {
            log.error("行政区划数据导入失败", e);
            result.setSuccess(false);
            result.setMessage("导入失败: " + e.getMessage());
            if (listener != null) {
                listener.onImportError(e);
            }
            throw new RuntimeException("行政区划数据导入失败", e);
        } finally {
            stopWatch.stop();
            result.setDuration(stopWatch.getTotalTimeMillis());
            log.info("导入耗时: {} ms", result.getDuration());

            if (listener != null && result.isSuccess()) {
                listener.onImportComplete(result);
            }
        }

        return result;
    }

    @Override
    public boolean needImport() {
        Long count = provinceMapper.selectCount(null);
        return count == null || count == 0;
    }

    /**
     * 加载行政区划数据
     * JSON格式: {"省份": {"城市": ["区县"]}, ...}
     */
    private Map<String, Map<String, List<String>>> loadRegionData(String filePath) throws Exception {
        try (InputStream inputStream = getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("文件不存在: " + filePath);
            }
            TypeReference<Map<String, Map<String, List<String>>>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(inputStream, typeRef);
        }
    }

    /**
     * 获取资源文件流
     */
    private InputStream getResourceAsStream(String filePath) {
        if (filePath.startsWith("classpath:")) {
            String resourcePath = filePath.substring("classpath:".length());
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }
            return getClass().getClassLoader().getResourceAsStream(resourcePath);
        } else {
            // 绝对路径或相对路径
            try {
                return new java.io.FileInputStream(filePath);
            } catch (Exception e) {
                throw new RuntimeException("无法读取文件: " + filePath, e);
            }
        }
    }

    /**
     * 处理省份数据
     */
    private Province processProvince(String provinceName, ImportResult result) {
        // 检查是否已存在
        Province existingProvince = null;
        if (checkDuplicate) {
            QueryWrapper<Province> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", provinceName);
            existingProvince = provinceMapper.selectOne(queryWrapper);
        }

        if (existingProvince != null) {
            log.debug("省份已存在，跳过导入: {}", provinceName);
            result.addWarning(String.format("省份重复: %s", provinceName));
            return existingProvince;
        }

        // 创建新省份
        Province province = new Province();
        province.setName(provinceName);
        province.setCountryId(defaultCountryId);

        try {
            provinceMapper.insert(province);
            result.setProvinceCount(result.getProvinceCount() + 1);
            log.debug("导入省份: {}", provinceName);
            return province;
        } catch (Exception e) {
            log.error("导入省份失败: {}", provinceName, e);
            result.addWarning(String.format("导入省份失败: %s - %s", provinceName, e.getMessage()));
            return null;
        }
    }

    /**
     * 处理城市和区县数据
     */
    private void processCitiesAndDistricts(Province province, Map<String, List<String>> citiesMap,
                                           ImportResult result, ImportProgressListener listener,
                                           String provinceName) {
        if (citiesMap == null || citiesMap.isEmpty()) {
            log.warn("省份 {} 没有城市数据", provinceName);
            return;
        }

        int cityIndex = 0;
        int totalCities = citiesMap.size();

        for (Map.Entry<String, List<String>> cityEntry : citiesMap.entrySet()) {
            cityIndex++;
            String cityName = cityEntry.getKey();
            List<String> districts = cityEntry.getValue();

            if (listener != null) {
                listener.onCityProgress(provinceName, cityName, cityIndex, totalCities);
            }

            // 处理城市
            City city = processCity(cityName, province.getId(), result);
            if (city == null) {
                log.warn("城市处理失败: {} - {}", provinceName, cityName);
                continue;
            }

            // 处理区县
            processDistricts(city, districts, result, listener, cityName);
        }
    }

    /**
     * 处理城市数据
     */
    private City processCity(String cityName, Long provinceId, ImportResult result) {
        // 检查是否已存在
        City existingCity = null;
        if (checkDuplicate) {
            QueryWrapper<City> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", cityName);
            queryWrapper.eq("province_id", provinceId);
            existingCity = cityMapper.selectOne(queryWrapper);
        }

        if (existingCity != null) {
            log.debug("城市已存在，跳过导入: {} (省份ID: {})", cityName, provinceId);
            result.addWarning(String.format("城市重复: %s (省份ID: %d)", cityName, provinceId));
            return existingCity;
        }

        // 创建新城市
        City city = new City();
        city.setName(cityName);
        city.setProvinceId(provinceId);

        try {
            cityMapper.insert(city);
            result.setCityCount(result.getCityCount() + 1);
            log.debug("导入城市: {} (省份ID: {})", cityName, provinceId);
            return city;
        } catch (Exception e) {
            log.error("导入城市失败: {} (省份ID: {})", cityName, provinceId, e);
            result.addWarning(String.format("导入城市失败: %s - %s", cityName, e.getMessage()));
            return null;
        }
    }

    /**
     * 处理区县数据
     */
    private void processDistricts(City city, List<String> districts, ImportResult result,
                                  ImportProgressListener listener, String cityName) {
        if (districts == null || districts.isEmpty()) {
            log.warn("城市 {} 没有区县数据", cityName);
            return;
        }

        int districtIndex = 0;
        int totalDistricts = districts.size();

        for (String districtName : districts) {
            districtIndex++;

            if (listener != null) {
                listener.onDistrictProgress(cityName, districtName, districtIndex, totalDistricts);
            }

            // 处理区县
            processDistrict(districtName, city.getId(), result);
        }
    }

    /**
     * 处理区县数据
     */
    private void processDistrict(String districtName, Long cityId, ImportResult result) {
        // 检查是否已存在
        District existingDistrict = null;
        if (checkDuplicate) {
            QueryWrapper<District> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("name", districtName);
            queryWrapper.eq("city_id", cityId);
            existingDistrict = districtMapper.selectOne(queryWrapper);
        }

        if (existingDistrict != null) {
            log.debug("区县已存在，跳过导入: {} (城市ID: {})", districtName, cityId);
            result.addWarning(String.format("区县重复: %s (城市ID: %d)", districtName, cityId));
            return;
        }

        // 创建新区县
        District district = new District();
        district.setName(districtName);
        district.setCityId(cityId);

        try {
            districtMapper.insert(district);
            result.setDistrictCount(result.getDistrictCount() + 1);
            log.debug("导入区县: {} (城市ID: {})", districtName, cityId);
        } catch (Exception e) {
            log.error("导入区县失败: {} (城市ID: {})", districtName, cityId, e);
            result.addWarning(String.format("导入区县失败: %s - %s", districtName, e.getMessage()));
        }
    }
}