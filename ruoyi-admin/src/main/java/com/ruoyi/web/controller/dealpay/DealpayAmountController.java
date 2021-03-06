package com.ruoyi.web.controller.dealpay;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.ruoyi.alipay.domain.AlipayAmountEntity;
import com.ruoyi.alipay.service.IAlipayAmountEntityService;
import com.ruoyi.common.constant.StaticConstants;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.framework.util.DictionaryUtils;
import com.ruoyi.framework.util.ShiroUtils;
import com.ruoyi.system.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.dealpay.domain.DealpayAmountEntity;
import com.ruoyi.dealpay.service.IDealpayAmountService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 手动加扣款记录Controller
 *
 * @author ruoyi
 * @date 2020-04-03
 */
@Controller
@RequestMapping("/dealpay/dealDeduct")
public class DealpayAmountController extends BaseController {
    private String prefix = "dealpay/dealDeduct";
    private String finance_prefix = "dealpay/finance";

    @Autowired
    private IDealpayAmountService dealpayAmountService;
    @Autowired
    private DictionaryUtils dictionaryUtils;

    @GetMapping()
    public String dealDeduct() {
        return prefix + "/dealDeduct";
    }

    /**
     * 查询手动加扣款记录列表
     */
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(DealpayAmountEntity dealpayAmountEntity) {
        startPage();
        List<DealpayAmountEntity> list = dealpayAmountService.selectDealpayAmountList(dealpayAmountEntity);
        return getDataTable(list);
    }

    /**
     * 导出手动加扣款记录列表
     */
    @Log(title = "加减款记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(DealpayAmountEntity dealpayAmountEntity) {
        List<DealpayAmountEntity> list = dealpayAmountService.selectDealpayAmountList(dealpayAmountEntity);
        ExcelUtil<DealpayAmountEntity> util = new ExcelUtil<DealpayAmountEntity>(DealpayAmountEntity.class);
        return util.exportExcel(list, "deduct");
    }

    /**
     * 修改手动加扣款记录
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        DealpayAmountEntity dealpayAmount = dealpayAmountService.selectDealpayAmountById(id);
        mmap.put("dealpayAmount", dealpayAmount);
        return prefix + "/edit";
    }

    /**
     * 修改保存手动加扣款记录
     */
    @Log(title = "手动加扣款记录", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(DealpayAmountEntity dealpayAmount) {
        return toAjax(dealpayAmountService.updateDealpayAmount(dealpayAmount));
    }

    /**
     * 删除手动加扣款记录
     */
    @Log(title = "手动加扣款记录", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(dealpayAmountService.deleteDealpayAmountByIds(ids));
    }

    /*财务处理加减款的controller逻辑处理*/

    @GetMapping("/view")
    public String financeView() {
        return finance_prefix + "/deduct";
    }



    /*财务处理逻辑*/
    /**
     * 财务查询卡商加减款记录
     */
    @PostMapping("/finance/list")
    @ResponseBody
    public TableDataInfo financeList(DealpayAmountEntity dealpayAmountEntity) {
        startPage();
        List<DealpayAmountEntity> list = dealpayAmountService.selectDealpayAmountList(dealpayAmountEntity);
        return getDataTable(list);
    }

    /**
     * 显示加减款记录详情
     */
    @GetMapping("/show/detail/{id}")
    public String showDetail(@PathVariable("id") Long id, ModelMap mmap) {
        DealpayAmountEntity dealpayAmount = dealpayAmountService.selectDealpayAmountById(id);
        mmap.put("dealpayAmount", dealpayAmount);
        return finance_prefix + "/edit";
    }



    /**
     * 财务审核加减款记录
     */
    @Log(title = "财务管理", businessType = BusinessType.UPDATE)
    @PostMapping("/dealpay/approval")
    @ResponseBody
    public AjaxResult apporval(DealpayAmountEntity dealpayAmountEntity) {
        // 获取当前的用户
        SysUser currentUser = ShiroUtils.getSysUser();
        //获取alipay处理接口URL
        String ipPort = dictionaryUtils.getApiUrlPath(StaticConstants.DealPAY_IP_URL_KEY, StaticConstants.DealPAY_IP_URL_VALUE);
        String urlPath = dictionaryUtils.getApiUrlPath(StaticConstants.DealPAY_SERVICE_API_KEY, StaticConstants.DealPAY_SERVICE_API_VALUE_5);
        Map<String, Object> mapParam = Collections.synchronizedMap(Maps.newHashMap());
        mapParam.put("id", dealpayAmountEntity.getId());
        mapParam.put("userId", dealpayAmountEntity.getUserId());
        mapParam.put("amount", dealpayAmountEntity.getAmount());
        mapParam.put("orderStatus", dealpayAmountEntity.getOrderStatus());//通过
        mapParam.put("orderId", dealpayAmountEntity.getOrderId());//订单号
        mapParam.put("approval", currentUser.getLoginName());//审核人
        mapParam.put("comment", dealpayAmountEntity.getComment());//审核人
        return HttpUtils.adminRequest2Gateway(mapParam, ipPort + urlPath);
    }

    /**
     * 财务审核加减款记录
     */
    @Log(title = "加减款记录", businessType = BusinessType.UPDATE)
    @PostMapping("/dealpay/reject")
    @ResponseBody
    public AjaxResult reject(DealpayAmountEntity dealpayAmountEntity) {
        // 获取当前的用户
        SysUser currentUser = ShiroUtils.getSysUser();
        //获取alipay处理接口URL
        String ipPort = dictionaryUtils.getApiUrlPath(StaticConstants.DealPAY_IP_URL_KEY, StaticConstants.DealPAY_IP_URL_VALUE);
        String urlPath = dictionaryUtils.getApiUrlPath(StaticConstants.DealPAY_SERVICE_API_KEY, StaticConstants.DealPAY_SERVICE_API_VALUE_5);
        Map<String, Object> mapParam = Collections.synchronizedMap(Maps.newHashMap());
        mapParam.put("id", dealpayAmountEntity.getId());
        mapParam.put("userId", dealpayAmountEntity.getUserId());
        mapParam.put("amount", dealpayAmountEntity.getAmount());
        mapParam.put("orderStatus", dealpayAmountEntity.getOrderStatus());//拒绝
        mapParam.put("orderId", dealpayAmountEntity.getOrderId());//订单号
        mapParam.put("approval", currentUser.getLoginName());//审核人
        mapParam.put("comment", dealpayAmountEntity.getComment());
        return HttpUtils.adminRequest2Gateway(mapParam, ipPort + urlPath);
    }

}
