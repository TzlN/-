package com.quxin.freshfun.controller.convertutils;

import java.text.ParseException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.quxin.freshfun.utils.DateUtils;
@Component
public class String2Long implements Converter<String, Long>{

	@Override
	public Long convert(String datetime) {
		Long timestamp = null;
		if(datetime !=null){
			try {
				timestamp = DateUtils.stringToLong(datetime, "yyyy-MM-dd HH:mm:ss")/1000;
			} catch (ParseException e) {
				e.printStackTrace();
			}
			System.out.println(datetime);
		}
		return timestamp;
	}
	
}
