package xyz.yaungyue.secondhand.listener;

import xyz.yaungyue.secondhand.model.dto.ImportResult;

/**
 * 行政区划导入进度监听器接口
 *
 * @author yaungyue
 * @date 2026-03-03
 */
public interface ImportProgressListener {

    /**
     * 导入开始
     *
     * @param totalProvinces 总省份数量
     */
    void onImportStart(int totalProvinces);

    /**
     * 处理进度更新
     *
     * @param currentProvince 当前处理的省份索引（从1开始）
     * @param provinceName 当前处理的省份名称
     * @param totalProvinces 总省份数量
     */
    void onProvinceProgress(int currentProvince, String provinceName, int totalProvinces);

    /**
     * 城市处理进度
     *
     * @param provinceName 省份名称
     * @param cityName 城市名称
     * @param currentCity 当前城市索引
     * @param totalCities 总城市数
     */
    void onCityProgress(String provinceName, String cityName, int currentCity, int totalCities);

    /**
     * 区县处理进度
     *
     * @param cityName 城市名称
     * @param districtName 区县名称
     * @param currentDistrict 当前区县索引
     * @param totalDistricts 总区县数
     */
    void onDistrictProgress(String cityName, String districtName, int currentDistrict, int totalDistricts);

    /**
     * 导入完成
     *
     * @param result 导入结果
     */
    void onImportComplete(ImportResult result);

    /**
     * 导入失败
     *
     * @param error 异常信息
     */
    void onImportError(Exception error);
}