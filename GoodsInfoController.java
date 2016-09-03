package com.quxin.freshfun.controller.goods;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.quxin.freshfun.model.GoodsTypePOJO;
import com.quxin.freshfun.model.ImgUtilsPOJO;
import com.quxin.freshfun.model.TestConvert;
import com.quxin.freshfun.model.goods.GoodsDetail;
import com.quxin.freshfun.model.goods.GoodsPOJO;
import com.quxin.freshfun.service.goods.GoodsService;
import com.quxin.freshfun.service.goods.GoodsTypeService;
import com.quxin.freshfun.utils.DateUtils;
import com.quxin.freshfun.utils.UploadUtils;

@Controller
@RequestMapping("/")
public class GoodsInfoController {
	
	private static String TIMEFORMAT = "yyyy-MM-dd HH:mm:ss";
	private static String IMAGEIP;
	
	@Value("${rip}")
	public void setRip(String value) {
		IMAGEIP = value;
	}
	@Autowired
	private GoodsService goodsService;

	@Autowired
	private GoodsTypeService goodsTypeService;
	
	/**
	 * 商品列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/goodsList")
	public String goodsList(HttpServletRequest request){
		//获取查询条件
		Map<String, Object> queryCondition = getQueryCondition(request);
		//1.设置分页条件
		Integer total = goodsService.count(queryCondition);
		String pageStr=request.getParameter("currentPage");
		String pageSizeStr=request.getParameter("pageSize");
		Integer page = null;
		Integer pageSize = null ;
		Integer totalPage = null;
		Integer start = null;
		//1.1设置页面大小
		if(pageSizeStr!=null && !"".equals(pageSizeStr)){
			pageSize = Integer.parseInt(pageSizeStr);
		}else{
			pageSize = 10;
		}
		totalPage =total%pageSize == 0 ? total/pageSize : total/pageSize +1;
		//1.2设置开始页
		if(pageStr !=null && !"".equals(pageStr)){
			page = Integer.parseInt(pageStr);
			if(page >totalPage){
				page = totalPage;
			}
			if(page == 0 ){
				page = 1;
			}
		}else{
			page =1;
		}
		//1.3设置记录开始数
		start = (page-1)*pageSize;
		
		queryCondition.put("pageSize", pageSize);
		queryCondition.put("start", start);
		
		List<GoodsPOJO> goodsPOJOs = new ArrayList<>();
		goodsPOJOs = goodsService.findMysqlList(queryCondition);
		//设置页面回显
		request.setAttribute("goodsList", goodsPOJOs);
		//分页信息
		request.setAttribute("page", page);
		request.setAttribute("pageSize", pageSize);
		request.setAttribute("totalPage", totalPage);
		request.setAttribute("totalRecords", total);
		
		//查询条件
		request.setAttribute("goodsName", queryCondition.get("goodsName"));
		request.setAttribute("storeId", queryCondition.get("storeId"));
		Long gmtCreateTimeStamp = (Long) queryCondition.get("gmtCreate"); 
		if(gmtCreateTimeStamp!=null){
			try {
				request.setAttribute("gmtCreate", DateUtils.longToString(gmtCreateTimeStamp,TIMEFORMAT));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return "/goods/goodsList";
	}

	/**
	 * 跳转新增页面
	 * @param request
	 * @return
	 */
	@RequestMapping("/toAddGoods")
	public String toAddGoods(HttpServletRequest request ){
		//1.商品类别
		List<GoodsTypePOJO> goodsTypes = new ArrayList<>();
		goodsTypes = goodsTypeService.findAll();
		request.setAttribute("goodsTypes", goodsTypes);
		return "/goods/addGoods";
	}
	
	/**
	 * 商品下架
	 * @return
	 */
	@RequestMapping("/removeGoods")
	public String removeGoods(@RequestParam(value="id",required =false ,defaultValue="-1") Integer id){
		if(id!=-1){
			goodsService.removeGoods(id);
		}
		return "redirect:goodsList.do";
	}
	
	/**
	 * 跳转编辑页面
	 * @param request
	 * @param id 商品id
	 * @return
	 */
	@RequestMapping("/toUpdateGoods")
	public String toUpdateGoods(HttpServletRequest request ,Integer id , HttpSession session){
		//1.商品类别
		List<GoodsTypePOJO> goodsTypes = new ArrayList<>();
		goodsTypes = goodsTypeService.findAll();
		request.setAttribute("goodsTypes", goodsTypes);
		
		//2.根据商品Id查询出主要的商品信息
		GoodsPOJO goods = goodsService.getGoodsById(id);
		goods.setGoodsTypeIds((goodsService.getTypeGoodsIds(id)));
		GoodsDetail gm = goodsService.getGoodsByGoodsId(id);
		
		//3时间显示处理	--
		try {
			String gmtCreateStr = DateUtils.longToString(goods.getGmtCreate()*1000, TIMEFORMAT);
			String gmtModifiedStr =DateUtils.longToString(goods.getGmtModified()*1000, TIMEFORMAT);
			String pickBirthdate= DateUtils.longToString(gm.getGoodsBirthdate()*1000, TIMEFORMAT);
			String pickOutdate= DateUtils.longToString(gm.getGoodsOutdate()*1000, TIMEFORMAT);
			
			request.setAttribute("gmtCreateStr", gmtCreateStr);
			request.setAttribute("gmtModifiedStr", gmtModifiedStr);
			request.setAttribute("pickBirthdate", pickBirthdate);
			request.setAttribute("pickOutdate", pickOutdate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//4.获取图片地址
		String rootPath =IMAGEIP;
		String detailImgPaths = gm.getDetailImgPath();
		String carouselImgPaths = gm.getCarouselImgPath();
		if(detailImgPaths!=null && !"".equals(detailImgPaths)){
			List<ImgUtilsPOJO> imgPaths = new ArrayList<>();
			for(String img : detailImgPaths.split(",")){
				String imgView = IMAGEIP +img;
				String imgPath = img;
				ImgUtilsPOJO imgUtil = new ImgUtilsPOJO(imgView,imgPath);
				imgPaths.add(imgUtil);
			}
			request.setAttribute("detailImgPaths", imgPaths);
		}
		if(carouselImgPaths!=null){
			List<ImgUtilsPOJO> imgPaths = new ArrayList<>();
			for(String img : carouselImgPaths.split(",")){
				String imgView = IMAGEIP +img;
				String imgPath = img;
				ImgUtilsPOJO imgUtil = new ImgUtilsPOJO(imgView,imgPath);
				imgPaths.add(imgUtil);
			}
			request.setAttribute("carouselImgPaths", imgPaths);
		}
		request.setAttribute("indexPicture", rootPath+goods.getGoodsImg());
		request.setAttribute("standardImgPath", rootPath+gm.getStandardImgPath());
		
		//5描述处理
		String[] des =  gm.getDes().split("@`");
		if(des!=null && des.length>1){
			String title = des[0];
			String titleDes = des[1];
			String editer = des[2];
			request.setAttribute("title", title);
			request.setAttribute("titleDes", titleDes);
			request.setAttribute("editer", editer);
		}
		
		request.setAttribute("goods", goods);
		request.setAttribute("gm", gm);
		
		return "/goods/updateGoods";
	}
	
	/**
	 * 保存商品信息
	 * @param request
	 * @param goods 商品主要信息
	 * @param gm 商品详情信息
	 * @return
	 */
	@RequestMapping("/saveGoods")
	public String saveGoods(HttpServletRequest request ,GoodsPOJO goods ,GoodsDetail gm){
		//1.处理日期
		String pickBirthdate = request.getParameter("pickBirthdate");
		String pickOutdate = request.getParameter("pickOutdate");
		try {
			Long goodsBirthdate = DateUtils.stringToLong(pickBirthdate, TIMEFORMAT)/1000;
			Long goodsOutdate = DateUtils.stringToLong(pickOutdate, TIMEFORMAT)/1000;
			gm.setGoodsBirthdate(goodsBirthdate);
			gm.setGoodsOutdate(goodsOutdate);
			goods.setGmtCreate(System.currentTimeMillis()/1000);//创建时间
			goods.setGmtModified(System.currentTimeMillis()/1000);//修改时间--字段为非空,新增时修改时间为创建时间
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		//2.处理商品分类信息
		List<Integer> goodsTypeIds = new ArrayList<>();
		String[] goodsTypes = request.getParameterValues("goodsTypes");
		if(goodsTypes !=null && !"".equals(goodsTypes)){
			for(String id : goodsTypes){
				goodsTypeIds.add(Integer.parseInt(id));
			}
		}
		//3.文件上传
		try {
			goods.setGoodsImg(UploadUtils.uploadOne(request, "indexFile"));
			gm.setStandardImgPath((UploadUtils.uploadOne(request, "standardImg")));
			gm.setCarouselImgPath(UploadUtils.upload(request, "carousel"));
			gm.setDetailImgPath(UploadUtils.upload(request, "detail"));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TODO 暂时不做
		goods.setMerchantProxyId(1);
		
		//4.处理描述
		String title = request.getParameter("title");
		String titleDes = request.getParameter("titleDes");
		String editer = request.getParameter("editer");
		String des = title+"@`"+titleDes+"@`"+editer;
		gm.setDes(des);
		
		//5.处理金额
		Double shopPriceD = Double.parseDouble(goods.getShopPriceString())*100;
		goods.setShopPrice(shopPriceD.intValue());
		Double marketPriceD = Double.parseDouble(goods.getMarketPriceString())*100;
		goods.setMarketPrice(marketPriceD.intValue());
		
		goodsService.addGoods(goods, gm,goodsTypeIds);
		return "redirect:goodsList.do";
	}
	
	
	/**
	 * 保存商品信息
	 * @param request
	 * @param goods 商品主要信息
	 * @param gm 商品详情信息
	 * @return
	 */
	@RequestMapping("/updateGoods")
	public String updateGoods(HttpServletRequest request ,GoodsPOJO goods ,GoodsDetail gm){
		//1.处理页面时间
		String pickBirthdate = request.getParameter("pickBirthdate");
		String pickOutdate = request.getParameter("pickOutdate");
		SimpleDateFormat sdf = new SimpleDateFormat(TIMEFORMAT);
		Long goodsBirthdate = null;
		Long goodsOutdate = null;
		try {
			goodsBirthdate = sdf.parse(pickBirthdate).getTime()/1000;
			goodsOutdate = sdf.parse(pickOutdate).getTime()/1000;
			goods.setGmtModified(System.currentTimeMillis()/1000);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		gm.setGoodsBirthdate(goodsBirthdate);
		gm.setGoodsOutdate(goodsOutdate);
		//2.商品类型
		List<Integer> goodsTypeIds = new ArrayList<>();
		String[] goodsTypes = request.getParameterValues("goodsTypes");
		if(goodsTypes !=null && !"".equals(goodsTypes)){
			for(String id : goodsTypes){
				goodsTypeIds.add(Integer.parseInt(id));
			}
		}
		//3.图片上传
		try {
			//首页图片
			String indexFile = UploadUtils.uploadOne(request, "indexFile");
			if(!"".equals(indexFile))
				goods.setGoodsImg(indexFile);
			//规格图片
			String standardImg = UploadUtils.uploadOne(request, "standardImg");
			if(!"".equals(standardImg))
				gm.setStandardImgPath(standardImg);
			//轮播原来的图片
			String[] carouselImgs = request.getParameterValues("carousel_file");
			//轮播新的图片
			String newCarouse = UploadUtils.upload(request, "carousel");
			StringBuffer oldCarouse = new StringBuffer("");
			if(carouselImgs!=null){
				for(String img : carouselImgs){
					oldCarouse.append(img+",");
				}
			}
			//判断是否有新的图片添加
			if(!"".equals(newCarouse)){
				gm.setCarouselImgPath(oldCarouse+newCarouse);
			}else{
				//判断旧的图片有没有被删除完
				if(oldCarouse !=null && !"".equals(oldCarouse.toString())){
					gm.setCarouselImgPath(oldCarouse.deleteCharAt(oldCarouse.length()-1).toString());
				}else{
					//删除完了之后就用""代替
					gm.setCarouselImgPath(null);
				}
			}
			//详情原来的图片
			String[] detailImgs = request.getParameterValues("detail_file");
			//详情新的图片
			String newDetail = UploadUtils.upload(request, "detail");
			StringBuffer oldDetail = new StringBuffer("");
			if(detailImgs!=null){
				for(String img : detailImgs){
					oldDetail.append(img+",");
				}
			}
			//存在新的图片就用旧的图片追加新的图片路径,否则就取出","
			if(!"".equals(newDetail)){
				gm.setDetailImgPath(oldDetail+newDetail);
			}else{
				if(oldDetail != null && !"".equals(oldDetail.toString())){
					gm.setDetailImgPath(oldDetail.deleteCharAt(oldDetail.length()-1).toString());
				}else{
					gm.setDetailImgPath(null);
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//4.金额处理
		Double shopPriceD = Double.parseDouble(goods.getShopPriceString())*100;
		goods.setShopPrice(shopPriceD.intValue());
		Double marketPriceD = Double.parseDouble(goods.getMarketPriceString())*100;
		goods.setMarketPrice(marketPriceD.intValue());
		//5.处理描述
		String title = request.getParameter("title");
		String titleDes = request.getParameter("titleDes");
		String editer = request.getParameter("editer");
		
		String des = title+"@`"+titleDes+"@`"+editer;
		gm.setDes(des);
		
		goodsService.updateGoods(goods, gm,goodsTypeIds);
		return "redirect:goodsList.do";
	}
	
	
	@RequestMapping("/upload")
	public String upload(HttpServletRequest request){
		String imgPath = null;
		try {
//			imgPath = UploadUtils.uploadOne(request,"file");
			imgPath = UploadUtils.upload(request,"file");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		request.setAttribute("imagesPath", imgPath);
		String[] imgs = imgPath.split(",");
		List<String> paths = new ArrayList<>();
		for(String img : imgs){
			paths.add(IMAGEIP+img);
		}
		request.setAttribute("imgs", paths);
		return "/uploadSuccess";
	}
	
	/****************************************工具方法***************************************************/
	/**
	 * 查询条件拼接
	 * @param req
	 * @return
	 */
	private Map<String, Object> getQueryCondition(HttpServletRequest req) {
		Map<String, Object> map = new HashMap<>();
		String goodsName = req.getParameter("goodsName");
		String storeId = req.getParameter("storeId");
		String gmtCreate = req.getParameter("gmtCreate");
		
		if(goodsName != null && !"".equals(goodsName)){
			map.put("goodsName", goodsName);
		}
		if(storeId != null && !"".equals(storeId)){
			map.put("storeId", storeId);
		}
		if(gmtCreate != null && !"".equals(gmtCreate)){
			try {
				map.put("gmtCreate", DateUtils.stringToLong(gmtCreate,TIMEFORMAT)/1000);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		return map;
	}
	
	@ResponseBody
	@RequestMapping("/validateGoodsName")
	public Map<String, Object> validateGoodsName(String goodsName , String id ,HttpServletResponse response){
		String message ="";
		Integer count = 0 ;
		Map<String, Object> map =  new HashMap<>();
		if(id !=null && !"".equals(id)){
			count = goodsService.validateGoodsName(goodsName , id);
		}else{
			count = goodsService.validateGoodsName(goodsName , null);
		}
		if(count < 1){
			message = "yes";//可用
		}else{
			message = "no";//不可用
		}
		map.put("msg", message);
		return map;
	}
	
	
	@RequestMapping("/convert")
	public String testconvert(Map<String, Object> map,TestConvert c){
		map.put("testConvert", c);
		return "/convert";
	}
}
