package xyz.yaungyue.secondhand.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import xyz.yaungyue.secondhand.model.dto.request.AddressCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.AddressUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.response.AddressVO;
import xyz.yaungyue.secondhand.model.dto.response.ApiResponse;
import xyz.yaungyue.secondhand.model.dto.response.ProvinceVO;
import xyz.yaungyue.secondhand.model.dto.response.CityVO;
import xyz.yaungyue.secondhand.model.dto.response.DistrictVO;
import xyz.yaungyue.secondhand.service.AddressService;
import xyz.yaungyue.secondhand.service.ProvinceService;
import xyz.yaungyue.secondhand.service.CityService;
import xyz.yaungyue.secondhand.service.DistrictService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 地址管理控制器
 *
 * @author yaungyue
 * @date 2026-02-28
 */
@Slf4j
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "地址管理", description = "收货地址的增删改查等接口")
public class AddressController {

    private final AddressService addressService;
    private final ProvinceService provinceService;
    private final CityService cityService;
    private final DistrictService districtService;

    /**
     * 创建地址
     *
     * @param request 地址创建请求
     * @return 创建的地址信息
     */
    @PostMapping
    @SaCheckLogin
    @Operation(summary = "创建地址")
    public ApiResponse<AddressVO> createAddress(@Valid @RequestBody AddressCreateRequest request) {
        log.info("创建地址请求: {}", request);
        AddressVO addressVO = addressService.createAddress(request);
        return ApiResponse.success(addressVO);
    }

    /**
     * 更新地址
     *
     * @param id      地址 ID
     * @param request 地址更新请求
     * @return 更新后的地址信息
     */
    @PutMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "更新地址")
    public ApiResponse<AddressVO> updateAddress(
            @PathVariable @Parameter(description = "地址ID", example = "1") Long id,
            @Valid @RequestBody AddressUpdateRequest request) {
        log.info("更新地址请求，ID: {}, 请求体: {}", id, request);
        AddressVO addressVO = addressService.updateAddress(id, request);
        return ApiResponse.success(addressVO);
    }

    /**
     * 删除地址
     *
     * @param id 地址 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "删除地址")
    public ApiResponse<Void> deleteAddress(
            @PathVariable @Parameter(description = "地址ID", example = "1") Long id) {
        log.info("删除地址请求，ID: {}", id);
        addressService.deleteAddress(id);
        return ApiResponse.success();
    }

    /**
     * 获取当前用户的地址列表
     *
     * @return 地址列表
     */
    @GetMapping
    @SaCheckLogin
    @Operation(summary = "获取当前用户的地址列表")
    public ApiResponse<List<AddressVO>> getAddressList() {
        log.info("获取当前用户地址列表");
        List<AddressVO> addressList = addressService.getAddressListByCurrentUser();
        return ApiResponse.success(addressList);
    }

    /**
     * 获取地址详情
     *
     * @param id 地址 ID
     * @return 地址详情
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "获取地址详情")
    public ApiResponse<AddressVO> getAddressById(
            @PathVariable @Parameter(description = "地址ID", example = "1") Long id) {
        log.info("获取地址详情，ID: {}", id);
        AddressVO addressVO = addressService.getAddressById(id);
        return ApiResponse.success(addressVO);
    }

    /**
     * 设置默认地址
     *
     * @param id 地址 ID
     * @return 操作结果
     */
    @PutMapping("/{id}/default")
    @SaCheckLogin
    @Operation(summary = "设置默认地址")
    public ApiResponse<Void> setDefaultAddress(
            @PathVariable @Parameter(description = "地址ID", example = "1") Long id) {
        log.info("设置默认地址请求，ID: {}", id);
        addressService.setDefaultAddress(id);
        return ApiResponse.success();
    }

    /**
     * 获取省份列表
     *
     * @param countryId 国家 ID，默认为 1
     * @return 省份列表
     */
    @GetMapping("/provinces")
    @Operation(summary = "获取省份列表")
    public ApiResponse<List<ProvinceVO>> getProvinces(
            @RequestParam(required = false, defaultValue = "1") Long countryId) {
        log.info("获取省份列表请求，countryId: {}", countryId);
        List<ProvinceVO> provinceVOs = provinceService.getByCountryId(countryId)
                .stream()
                .map(province -> {
                    ProvinceVO vo = new ProvinceVO();
                    vo.setId(province.getId());
                    vo.setCountryId(province.getCountryId());
                    vo.setName(province.getName());
                    return vo;
                })
                .collect(Collectors.toList());
        return ApiResponse.success(provinceVOs);
    }

    /**
     * 获取城市列表
     *
     * @param provinceId 省份 ID
     * @return 城市列表
     */
    @GetMapping("/cities")
    @Operation(summary = "获取城市列表")
    public ApiResponse<List<CityVO>> getCities(
            @RequestParam @NotNull Long provinceId) {
        log.info("获取城市列表请求，provinceId: {}", provinceId);
        List<CityVO> cityVOs = cityService.getByProvinceId(provinceId)
                .stream()
                .map(city -> {
                    CityVO vo = new CityVO();
                    vo.setId(city.getId());
                    vo.setProvinceId(city.getProvinceId());
                    vo.setName(city.getName());
                    return vo;
                })
                .collect(Collectors.toList());
        return ApiResponse.success(cityVOs);
    }

    /**
     * 获取区县列表
     *
     * @param cityId 城市 ID
     * @return 区县列表
     */
    @GetMapping("/districts")
    @Operation(summary = "获取区县列表")
    public ApiResponse<List<DistrictVO>> getDistricts(
            @RequestParam @NotNull Long cityId) {
        log.info("获取区县列表请求，cityId: {}", cityId);
        List<DistrictVO> districtVOs = districtService.getByCityId(cityId)
                .stream()
                .map(district -> {
                    DistrictVO vo = new DistrictVO();
                    vo.setId(district.getId());
                    vo.setCityId(district.getCityId());
                    vo.setName(district.getName());
                    return vo;
                })
                .collect(Collectors.toList());
        return ApiResponse.success(districtVOs);
    }
}