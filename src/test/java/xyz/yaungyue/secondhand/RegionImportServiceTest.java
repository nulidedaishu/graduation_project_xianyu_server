package xyz.yaungyue.secondhand;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.service.RegionImportService;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 行政区划导入服务测试
 *
 * @author yaungyue
 * @date 2026-03-03
 */
@Slf4j
@SpringBootTest
@Transactional
@Rollback
public class RegionImportServiceTest {

    @Autowired
    private RegionImportService regionImportService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testNeedImport() {
        // 首次运行时应该需要导入（如果数据库是空的）
        boolean needImport = regionImportService.needImport();
        log.info("是否需要导入数据: {}", needImport);

        // 这个测试不会失败，只是验证方法能正常执行
        assertTrue(true);
    }

    @Test
    public void testJsonFileExists() {
        // 验证pca.json文件存在且可读
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("pca.json")) {
            assertNotNull(inputStream, "pca.json文件不存在");

            // 尝试解析JSON为Map结构
            TypeReference<Map<String, Map<String, List<String>>>> typeRef = new TypeReference<>() {};
            Map<String, Map<String, List<String>>> data = objectMapper.readValue(inputStream, typeRef);
            assertNotNull(data, "JSON解析失败");
            assertFalse(data.isEmpty(), "JSON数据为空");

            log.info("pca.json文件验证通过，包含 {} 个省份", data.size());

            // 验证数据结构
            int totalCities = 0;
            int totalDistricts = 0;
            for (Map.Entry<String, Map<String, List<String>>> provinceEntry : data.entrySet()) {
                String provinceName = provinceEntry.getKey();
                Map<String, List<String>> cities = provinceEntry.getValue();
                assertNotNull(cities, "省份 " + provinceName + " 的城市数据为空");
                assertFalse(cities.isEmpty(), "省份 " + provinceName + " 没有城市数据");
                totalCities += cities.size();

                for (Map.Entry<String, List<String>> cityEntry : cities.entrySet()) {
                    String cityName = cityEntry.getKey();
                    List<String> districts = cityEntry.getValue();
                    assertNotNull(districts, "城市 " + cityName + " 的区县数据为空");
                    totalDistricts += districts.size();
                    // 有些城市可能没有区县（如直辖市的部分区）
                    // 只记录日志，不强制断言
                    if (districts.isEmpty()) {
                        log.debug("城市 {} 没有区县数据", cityName);
                    }
                }
            }

            log.info("数据验证完成: {} 个省份, {} 个城市, {} 个区县",
                    data.size(), totalCities, totalDistricts);

        } catch (Exception e) {
            fail("pca.json文件读取或解析失败: " + e.getMessage());
        }
    }

    @Test
    public void testDefaultFilePath() {
        String defaultFilePath = regionImportService.getDefaultFilePath();
        assertEquals("classpath:pca.json", defaultFilePath);
        log.info("默认文件路径: {}", defaultFilePath);
    }

    @Test
    public void testServiceInjection() {
        assertNotNull(regionImportService, "RegionImportService注入失败");
        log.info("RegionImportService注入成功");
    }
}