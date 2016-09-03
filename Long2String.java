package com.quxin.freshfun.controller.convertutils;

import java.text.ParseException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.quxin.freshfun.utils.DateUtils;
@Component
public class Long2String implements Converter<Long, String>{


	@Override
	public String convert(Long timestamp) {
		String timedate = null;
		if(timestamp!=null){
			try {
				timedate = DateUtils.longToString(timestamp*1000, "yyyy-MM-dd HH:mm:ss");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return timedate;
	}
	
}
