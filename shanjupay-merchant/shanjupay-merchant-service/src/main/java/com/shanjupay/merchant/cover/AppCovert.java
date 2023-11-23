package com.shanjupay.merchant.cover;


import com.shanjupay.merchant.api.dto.app.AppDTO;
import com.shanjupay.merchant.entity.App;

import org.apache.ibatis.annotations.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

import java.util.List;

@Mapper
    public interface AppCovert {
        AppCovert INSTANCE = Mappers.getMapper(AppCovert.class);
        AppDTO entity2dto(App entity);
        App dto2entity(AppDTO dto);
        List<AppDTO> listentity2dto(List<App> app);

}
