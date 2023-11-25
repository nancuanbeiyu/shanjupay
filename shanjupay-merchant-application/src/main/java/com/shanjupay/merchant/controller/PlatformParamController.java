package com.shanjupay.merchant.controller;

import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.api.port.PayChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "商户平台‐渠道和支付参数相关", tags = "商户平台‐渠道和支付参数", description = "商户平台‐渠道和支付参数相关")
@Slf4j
@RestController
public class PlatformParamController {
    @Reference
    private PayChannelService payChannelService;

    @ApiOperation("获取平台服务类型")
    @GetMapping(value = "/my/platform‐channels")
    public List<PlatformChannelDTO> queryPlatformChannel() {
        return payChannelService.queryPlatformChannel();
    }

    @ApiOperation("绑定服务类型")
    @PostMapping(value="/my/apps/{appId}/platform‐channels")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用id",name = "appId",dataType = "string",paramType =
                    "path"),
            @ApiImplicitParam(value = "服务类型code",name = "platformChannelCodes",dataType =
                    "string",paramType = "query")
    })
    public void bindPlatformForApp(@PathVariable("appId") String appId,
                                   @RequestParam("platformChannelCodes") String platformChannelCodes){
        payChannelService.bindPlatformChannelForApp(appId,platformChannelCodes);
    }
    @ApiOperation("查询应用是否绑定了某个服务类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "appId", value = "应用appId", required = true, dataType =
                    "String", paramType = "query"),
            @ApiImplicitParam(name = "platformChannel", value = "服务类型", required = true, dataType =
                    "String", paramType = "query")
    })
    @GetMapping("/my/merchants/apps/platformchannels")
    public int queryAppBindPlatformChannel(@RequestParam String appId, @RequestParam String
            platformChannel){
        return payChannelService.queryAppBindPlatformChannel(appId,platformChannel);
    }
    @ApiOperation("根据平台服务类型获取支付渠道列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "platformChannelCode", value = "服务类型编码", required =
                    true, dataType = "String", paramType = "path")
    })
    @GetMapping(value="/my/pay‐channels/platform‐channel/{platformChannelCode}")
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(@PathVariable String platformChannelCode){
        return payChannelService.queryPayChannelByPlatformChannel(platformChannelCode);
    }
}
