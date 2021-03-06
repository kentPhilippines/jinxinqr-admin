package com.ruoyi.web.controller.system;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.util.ShiroUtils;
import com.ruoyi.system.domain.HUOBI;
import com.ruoyi.system.domain.SysDictData;
import com.ruoyi.system.service.ISysDictDataService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据字典信息
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseController {
    private String prefix = "system/dict/data";

    @Autowired
    private ISysDictDataService dictDataService;

    @RequiresPermissions("system:dict:view")
    @GetMapping()
    public String dictData() {
        return prefix + "/data";
    }

    private Map cache = new ConcurrentHashMap();

    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:dict:export")
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SysDictData dictData) {
        List<SysDictData> list = dictDataService.selectDictDataList(dictData);
        ExcelUtil<SysDictData> util = new ExcelUtil<SysDictData>(SysDictData.class);
        return util.exportExcel(list, "字典数据");
    }

    /**
     * 新增字典类型
     */
    @GetMapping("/add/{dictType}")
    public String add(@PathVariable("dictType") String dictType, ModelMap mmap) {
        mmap.put("dictType", dictType);
        return prefix + "/add";
    }

    @GetMapping("/addManage/{dictType}")
    public String addManage(@PathVariable("dictType") String dictType, ModelMap mmap) {
        mmap.put("dictType", dictType);
        return prefix + "/addManage";
    }

    /**
     * 新增保存字典类型
     */
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @RequiresPermissions("system:dict:add")
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@Validated SysDictData dict) {
        dict.setCreateBy(ShiroUtils.getLoginName());
        return toAjax(dictDataService.insertDictData(dict));
    }

    /**
     * 修改字典类型
     */
    @GetMapping("/edit/{dictCode}")
    public String edit(@PathVariable("dictCode") Long dictCode, ModelMap mmap) {
        mmap.put("dict", dictDataService.selectDictDataById(dictCode));
        return prefix + "/edit";
    }

    @GetMapping("/editManage/{dictCode}")
    public String editManage(@PathVariable("dictCode") Long dictCode, ModelMap mmap) {
        mmap.put("dict", dictDataService.selectDictDataById(dictCode));
        return prefix + "/editManage";
    }

    /**
     * 修改保存字典类型
     */
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:dict:edit")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Validated SysDictData dict) {
        dict.setUpdateBy(ShiroUtils.getLoginName());
        return toAjax(dictDataService.updateDictData(dict));
    }

    private String MARK = "_";
    private String RATE_KEY = "usdtrate" + MARK;

    @PostMapping("/list")
    @RequiresPermissions("system:dict:list")
    @ResponseBody
    public TableDataInfo list(SysDictData dictData) {
        startPage();
        List<SysDictData> list = dictDataService.selectDictDataList(dictData);
        if (dictData.getDictType().equals("CNY_USDT")) {
            for (SysDictData data : list) {
                data.setDictValueTime(getHUOBIRateFee());
            }
        }
        return getDataTable(list);
    }

    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @RequiresPermissions("system:dict:remove")
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(dictDataService.deleteDictDataByIds(ids));
    }

    public String getHUOBIRateFee() {
        Set<String> set = cache.keySet();
        for (String key : set) {
            String[] split = key.split(MARK);
            String s = split[1];
            if (DateUtil.isExpired(DateUtil.parse(s).toJdkDate(), DateField.SECOND, 5, new Date())) {
                cache.remove(key);
            }
            ;
        }
        Object o = this.cache.get(RATE_KEY + DateUtils.getTime());
        if (null == o) {
            String urlbuy = "https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=2&currency=1&tradeType=buy&currPage=1&payMethod=0&acceptOrder=-1&country=&blockType=block&online=1&range=0&amount=";
            String rate = getRate(urlbuy);
            this.cache.put(RATE_KEY + DateUtils.getTime(), rate);
            return rate;
        } else {
            return "" + o;
        }
    }

    @PostMapping("/listRateInHUOBI")
    @ResponseBody
    public TableDataInfo listRateInHUOBI() {
        /**
         * 获取火币网费率作为最低和最高的 费率交易标准
         */
        List<HUOBI> list = new ArrayList<HUOBI>();
        HUOBI bigsell = new HUOBI();
        String urlsell = "https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=2&currency=1&tradeType=sell&currPage=1&payMethod=0&acceptOrder=-1&country=&blockType=block&online=1&range=0&amount=";
        bigsell.setId("1");
        bigsell.setRateType("大宗买入价格");
        bigsell.setPrice(getRate(urlsell));
        bigsell.setCaeateTime(DateUtils.getTime());
        list.add(bigsell);
        HUOBI bigbuy = new HUOBI();
        String urlbuy = "https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=2&currency=1&tradeType=buy&currPage=1&payMethod=0&acceptOrder=-1&country=&blockType=block&online=1&range=0&amount=";
        bigbuy.setId("2");
        bigbuy.setRateType("大宗出售价格");
        bigbuy.setPrice(getRate(urlbuy));
        bigbuy.setCaeateTime(DateUtils.getTime());
        list.add(bigbuy);
        HUOBI smalls = new HUOBI();
        String smallSell = "https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=2&currency=1&tradeType=sell&currPage=1&payMethod=0&acceptOrder=-1&country=&blockType=general&online=1&range=0&amount=";
        smalls.setId("3");
        smalls.setRateType("自选交易购买价格");
        smalls.setPrice(getRate(smallSell));
        smalls.setCaeateTime(DateUtils.getTime());
        list.add(smalls);
        HUOBI smallb = new HUOBI();
        String smallbull = "https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=2&currency=1&tradeType=buy&currPage=1&payMethod=0&acceptOrder=-1&country=&blockType=general&online=1&range=0&amount=";
        smallb.setId("4");
        smallb.setRateType("自选交易出售价格");
        smallb.setPrice(getRate(smallbull));
        smallb.setCaeateTime(DateUtils.getTime());
        list.add(smallb);
        return getDataTable(list);
    }


    String getRate(String url) {
        try {
            String sell = HttpUtil.get(url);
            JSONObject jsonObjectsell = JSONUtil.parseObj(sell);
            String codesell = jsonObjectsell.getStr("code");
            if ("200".contains(codesell)) {
                String date1sell = jsonObjectsell.getStr("data");
                JSONArray datesell = JSONUtil.parseArray(date1sell);
                Object[] objectssell = datesell.stream().toArray();
                Object objectsell = objectssell[0];
                JSONObject jsonObject1sell = JSONUtil.parseObj(objectsell);
                String pricesell = jsonObject1sell.getStr("price");
                return pricesell;
            }
        } catch (Exception e) {
            return "获取错误";
        }
        return "获取失败";
    }


}
