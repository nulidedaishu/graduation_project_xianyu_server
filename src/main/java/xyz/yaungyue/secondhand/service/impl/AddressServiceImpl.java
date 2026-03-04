package xyz.yaungyue.secondhand.service.impl;

import org.springframework.beans.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.yaungyue.secondhand.exception.BusinessException;
import xyz.yaungyue.secondhand.exception.ErrorCode;
import xyz.yaungyue.secondhand.mapper.AddressMapper;
import xyz.yaungyue.secondhand.model.dto.request.AddressCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.AddressUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.response.AddressVO;
import xyz.yaungyue.secondhand.model.entity.Address;
import xyz.yaungyue.secondhand.service.AddressService;
import xyz.yaungyue.secondhand.util.SaTokenUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import xyz.yaungyue.secondhand.service.ProvinceService;
import xyz.yaungyue.secondhand.service.CityService;
import xyz.yaungyue.secondhand.service.DistrictService;
import xyz.yaungyue.secondhand.model.entity.Province;
import xyz.yaungyue.secondhand.model.entity.City;
import xyz.yaungyue.secondhand.model.entity.District;

/**
* @author yaung
* @description 针对表【bus_address(收货地址表)】的数据库操作Service实现
* @createDate 2026-02-12 17:21:41
*/
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address>
    implements AddressService {

    private final ProvinceService provinceService;
    private final CityService cityService;
    private final DistrictService districtService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO createAddress(AddressCreateRequest request) {
        Long currentUserId = SaTokenUtil.getCurrentUserId();

        // 校验参数
        validateAddressRequest(request);

        // 创建地址实体
        Address address = new Address();
        BeanUtils.copyProperties(request, address);
        address.setUserId(currentUserId);
        address.setCreateTime(LocalDateTime.now());

        // 如果设置为默认地址，需要将用户的其他地址设置为非默认
        if (address.getIsDefault() == 1) {
            clearUserDefaultAddress(currentUserId);
        }

        // 保存地址
        boolean success = save(address);
        if (!success) {
            throw new BusinessException(ErrorCode.ADDRESS_CREATE_FAILED);
        }

        // 返回VO
        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO updateAddress(Long addressId,AddressUpdateRequest request) {
        Long currentUserId = SaTokenUtil.getCurrentUserId();

        // 检查地址是否存在且属于当前用户
        checkAddressPermission(addressId, currentUserId);

        // 校验参数
        validateAddressRequest(request);

        // 获取原地址
        Address address = getById(addressId);
        if (address == null) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // 更新地址信息
        BeanUtils.copyProperties(request, address);

        // 如果设置为默认地址，需要将用户的其他地址设置为非默认
        if (address.getIsDefault() == 1) {
            clearUserDefaultAddress(currentUserId);
        }

        // 更新地址
        boolean success = updateById(address);
        if (!success) {
            throw new BusinessException(ErrorCode.ADDRESS_UPDATE_FAILED);
        }

        // 返回VO
        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long addressId) {
        Long currentUserId = SaTokenUtil.getCurrentUserId();

        // 检查地址是否存在且属于当前用户
        checkAddressPermission(addressId, currentUserId);

        // 检查是否为默认地址（可选：不允许删除默认地址）
        Address address = getById(addressId);
        if (address != null && address.getIsDefault() == 1) {
            // 可选：如果不想允许删除默认地址，可以抛出异常
            // throw new BusinessException(ErrorCode.ADDRESS_DELETE_FAILED, "不能删除默认地址");
        }

        // 删除地址
        boolean success = removeById(addressId);
        if (!success) {
            throw new BusinessException(ErrorCode.ADDRESS_DELETE_FAILED);
        }
    }

    @Override
    public List<AddressVO> getAddressListByCurrentUser() {
        Long currentUserId = SaTokenUtil.getCurrentUserId();

        // 查询当前用户的所有地址
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, currentUserId)
                   .orderByDesc(Address::getIsDefault)  // 默认地址排前面
                   .orderByDesc(Address::getCreateTime); // 按创建时间倒序

        List<Address> addresses = list(queryWrapper);

        // 转换为VO列表
        return addresses.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressVO getAddressById(Long addressId) {
        Long currentUserId = SaTokenUtil.getCurrentUserId();

        // 检查地址是否存在且属于当前用户
        checkAddressPermission(addressId, currentUserId);

        Address address = getById(addressId);
        if (address == null) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long addressId) {
        Long currentUserId = SaTokenUtil.getCurrentUserId();

        // 检查地址是否存在且属于当前用户
        checkAddressPermission(addressId, currentUserId);

        // 获取地址
        Address address = getById(addressId);
        if (address == null) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // 如果已经是默认地址，直接返回
        if (address.getIsDefault() == 1) {
            return;
        }

        // 先将用户的所有地址设置为非默认
        clearUserDefaultAddress(currentUserId);

        // 再将指定地址设置为默认
        address.setIsDefault(1);
        boolean success = updateById(address);
        if (!success) {
            throw new BusinessException(ErrorCode.ADDRESS_SET_DEFAULT_FAILED);
        }
    }

    @Override
    public AddressVO getDefaultAddressByUserId(Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                   .eq(Address::getIsDefault, 1);

        Address address = getOne(queryWrapper);
        if (address == null) {
            return null;
        }

        return convertToVO(address);
    }

    @Override
    public boolean isAddressBelongsToCurrentUser(Long addressId) {
        Long currentUserId = SaTokenUtil.getCurrentUserId();
        return isAddressBelongsToUser(addressId, currentUserId);
    }

    @Override
    public boolean isAddressBelongsToUser(Long addressId, Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getId, addressId)
                   .eq(Address::getUserId, userId);

        return count(queryWrapper) > 0;
    }

    /**
     * 检查地址权限
     */
    private void checkAddressPermission(Long addressId, Long userId) {
        boolean belongsToUser = isAddressBelongsToUser(addressId, userId);
        if (!belongsToUser) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_BELONG_TO_USER);
        }
    }

    /**
     * 清除用户的默认地址（将所有地址设置为非默认）
     */
    private void clearUserDefaultAddress(Long userId) {
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId)
                   .eq(Address::getIsDefault, 1);

        List<Address> defaultAddresses = list(queryWrapper);
        if (!defaultAddresses.isEmpty()) {
            for (Address address : defaultAddresses) {
                address.setIsDefault(0);
            }
            updateBatchById(defaultAddresses);
        }
    }

    /**
     * 校验地址请求参数
     */
    private void validateAddressRequest(Object request) {
        // 这里可以添加更复杂的校验逻辑
        // 基本的参数校验已经通过@Valid注解完成
        // 这里主要校验业务逻辑，比如地址数量限制等

        Long currentUserId = SaTokenUtil.getCurrentUserId();

        // 只对创建请求检查地址数量限制
        if (request instanceof AddressCreateRequest) {
            // 检查用户地址数量是否达到上限
            LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Address::getUserId, currentUserId);
            long addressCount = count(queryWrapper);

            // 假设最大地址数量为20
            if (addressCount >= 20) {
                throw new BusinessException(ErrorCode.ADDRESS_MAX_LIMIT_REACHED);
            }
        }
        // 对于更新请求，不检查地址数量限制，因为只是更新现有地址

        // 验证省市区ID
        if (request instanceof AddressCreateRequest) {
            AddressCreateRequest createRequest = (AddressCreateRequest) request;
            validateRegionIds(createRequest.getProvinceId(), createRequest.getCityId(), createRequest.getDistrictId());
        } else if (request instanceof AddressUpdateRequest) {
            AddressUpdateRequest updateRequest = (AddressUpdateRequest) request;
            validateRegionIds(updateRequest.getProvinceId(), updateRequest.getCityId(), updateRequest.getDistrictId());
        }
    }

    /**
     * 验证省市区ID的有效性
     */
    private void validateRegionIds(Long provinceId, Long cityId, Long districtId) {
        // 验证省份是否存在
        Province province = provinceService.getById(provinceId);
        if (province == null) {
            throw new BusinessException(ErrorCode.PROVINCE_NOT_FOUND);
        }

        // 验证城市是否存在且属于该省份
        City city = cityService.getById(cityId);
        if (city == null) {
            throw new BusinessException(ErrorCode.CITY_NOT_FOUND);
        }
        if (!city.getProvinceId().equals(provinceId)) {
            throw new BusinessException(ErrorCode.CITY_NOT_BELONG_TO_PROVINCE);
        }

        // 验证区县是否存在且属于该城市
        District district = districtService.getById(districtId);
        if (district == null) {
            throw new BusinessException(ErrorCode.DISTRICT_NOT_FOUND);
        }
        if (!district.getCityId().equals(cityId)) {
            throw new BusinessException(ErrorCode.DISTRICT_NOT_BELONG_TO_CITY);
        }
    }

    /**
     * 将Address实体转换为AddressVO
     */
    private AddressVO convertToVO(Address address) {
        if (address == null) {
            return null;
        }

        AddressVO addressVO = new AddressVO();
        BeanUtils.copyProperties(address, addressVO);

        // 初始化省市区名称
        String provinceName = "";
        String cityName = "";
        String districtName = "";

        // 查询省份名称（如果ID不为null）
        if (address.getProvinceId() != null) {
            Province province = provinceService.getById(address.getProvinceId());
            if (province != null) {
                provinceName = province.getName();
                addressVO.setProvince(provinceName);
            }
        }

        // 查询城市名称（如果ID不为null）
        if (address.getCityId() != null) {
            City city = cityService.getById(address.getCityId());
            if (city != null) {
                cityName = city.getName();
                addressVO.setCity(cityName);
            }
        }

        // 查询区县名称（如果ID不为null）
        if (address.getDistrictId() != null) {
            District district = districtService.getById(address.getDistrictId());
            if (district != null) {
                districtName = district.getName();
                addressVO.setDistrict(districtName);
            }
        }

        // 构建完整地址
        String fullAddress = provinceName + cityName + districtName + address.getDetailAddress();
        addressVO.setFullAddress(fullAddress);

        return addressVO;
    }
}




