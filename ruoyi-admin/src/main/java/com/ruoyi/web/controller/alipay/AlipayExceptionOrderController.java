package com.ruoyi.web.controller.alipay;

import com.ruoyi.alipay.domain.AlipayExceptionOrder;
import com.ruoyi.alipay.service.IAlipayExceptionOrderService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拦截订单Controller
 * @author ruoyi
 * @date 2020-03-19
 */
@Controller
@RequestMapping("/alipay/export")
public class AlipayExceptionOrderController extends BaseController {
	private String prefix = "alipay/export";
	@Autowired
	private IAlipayExceptionOrderService alipayExceptionOrderService;
	@GetMapping()
	public String order() {
		return prefix + "/order";
	}
	/**
	 * 查询拦截订单列表
	 */
	@PostMapping("/list")
	@ResponseBody
	public TableDataInfo list(AlipayExceptionOrder alipayExceptionOrder) {
		startPage();
		List<AlipayExceptionOrder> list = alipayExceptionOrderService
				.selectAlipayExceptionOrderList(alipayExceptionOrder);
		return getDataTable(list);
	}
	/**
	 * 导出拦截订单列表
	 */
	@Log(title = "拦截订单", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	@ResponseBody
	public AjaxResult export(AlipayExceptionOrder alipayExceptionOrder) {
		List<AlipayExceptionOrder> list = alipayExceptionOrderService
				.selectAlipayExceptionOrderList(alipayExceptionOrder);
		ExcelUtil<AlipayExceptionOrder> util = new ExcelUtil<AlipayExceptionOrder>(AlipayExceptionOrder.class);
		return util.exportExcel(list, "order");
	}
	/**
	 * 新增拦截订单
	 */
	@GetMapping("/add")
	public String add() {
		return prefix + "/add";
	}
	/**
	 * 新增保存拦截订单
	 */
	@Log(title = "拦截订单", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	@ResponseBody
	public AjaxResult addSave(AlipayExceptionOrder alipayExceptionOrder) {
		return toAjax(alipayExceptionOrderService.insertAlipayExceptionOrder(alipayExceptionOrder));
	}
	/**
	 * 修改拦截订单
	 */
	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") Long id, ModelMap mmap) {
		AlipayExceptionOrder alipayExceptionOrder = alipayExceptionOrderService.selectAlipayExceptionOrderById(id);
		mmap.put("alipayExceptionOrder", alipayExceptionOrder);
		return prefix + "/edit";
	}
	/**
	 * 修改保存拦截订单
	 */
	@Log(title = "拦截订单", businessType = BusinessType.UPDATE)
	@PostMapping("/edit")
	@ResponseBody
	public AjaxResult editSave(AlipayExceptionOrder alipayExceptionOrder) {
		return toAjax(alipayExceptionOrderService.updateAlipayExceptionOrder(alipayExceptionOrder));
	}

	/**
	 * 删除拦截订单
	 */
	@Log(title = "拦截订单", businessType = BusinessType.DELETE)
	@PostMapping("/remove")
	@ResponseBody
	public AjaxResult remove(String ids) {
		return toAjax(alipayExceptionOrderService.deleteAlipayExceptionOrderByIds(ids));
	}


	@GetMapping("/accounting")
	public String accounting() {
		return prefix + "/accounting";
	}

}
