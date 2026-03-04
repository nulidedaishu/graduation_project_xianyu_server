package xyz.yaungyue.secondhand.service;

import xyz.yaungyue.secondhand.model.dto.request.AddressCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.AddressUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.response.AddressVO;
import xyz.yaungyue.secondhand.model.entity.Address;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yaung
* @description 针对表【bus_address(收货地址表)】的数据库操作Service
* @createDate 2026-02-12 17:21:41
*/
public interface AddressService extends IService<Address> {

    /**
     * 创建地址
     * @param request 地址创建请求
     * @return 地址VO
     */
    AddressVO createAddress(AddressCreateRequest request);

    /**
     * 更新地址
     * @param request 地址更新请求
     * @return 地址VO
     */
    AddressVO updateAddress(Long addressId,AddressUpdateRequest request);

    /**
     * 删除地址
     * @param addressId 地址ID
     */
    void deleteAddress(Long addressId);

    /**
     * 获取当前用户的地址列表
     * @return 地址VO列表
     */
    List<AddressVO> getAddressListByCurrentUser();

    /**
     * 获取地址详情
     * @param addressId 地址ID
     * @return 地址VO
     */
    AddressVO getAddressById(Long addressId);

    /**
     * 设置默认地址
     * @param addressId 地址ID
     */
    void setDefaultAddress(Long addressId);

    /**
     * 获取用户的默认地址
     * @param userId 用户ID
     * @return 地址VO
     */
    AddressVO getDefaultAddressByUserId(Long userId);

    /**
     * 检查地址是否属于当前用户
     * @param addressId 地址ID
     * @return 是否属于当前用户
     */
    boolean isAddressBelongsToCurrentUser(Long addressId);

    /**
     * 检查地址是否存在且属于指定用户
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 是否属于指定用户
     */
    boolean isAddressBelongsToUser(Long addressId, Long userId);
}
