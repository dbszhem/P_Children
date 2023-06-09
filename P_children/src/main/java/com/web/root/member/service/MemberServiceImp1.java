package com.web.root.member.service;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.root.kakaoPay.dto.ItemDTO;
import com.web.root.kakaoPay.dto.KakaoPaymentApproveResultDTO;
import com.web.root.kakaoPay.dto.KakaoPaymentOrderInfoDTO;
import com.web.root.member.dto.KakaoLoginDTO;
//github.com/ssp930/P_Children
import com.web.root.member.dto.MemberDTO;
import com.web.root.mybatis.kakao.KakaoPayMapper;
import com.web.root.mybatis.member.MemberMapper;

@Service
public class MemberServiceImp1 implements MemberService {

	@Autowired
	MemberMapper mapper;
	
	@Autowired
	CheckMailService cms;
	
	@Inject
	JavaMailSender mailSender;
	
	@Autowired
	KakaoPayMapper kakaoPayMapper;
	
	
	@Override
	public MemberDTO member_information(String id) {
		return mapper.member_information(id);
	}

	@Override
	public void modify_save(HttpServletRequest request) {
		MemberDTO dto = new MemberDTO();
		dto.setId(request.getParameter("id"));
		dto.setPwd(request.getParameter("re_pwd"));
		dto.setNickname(request.getParameter("nickname"));
		dto.setPhone(request.getParameter("phone"));
		dto.setAddr(request.getParameter("addr"));

		mapper.modify_save(dto);
	}
	
	@Override
	public void member_board(Model model, int num) {
		int pageLetter = 5; 
		int allCount = mapper.selectMember_boardCount(); 
		int repeat = allCount/pageLetter;  
		if(allCount % pageLetter != 0)
			repeat += 1;
		int end = num * pageLetter;
		int start = end + 1 - pageLetter;
		model.addAttribute("repeat", repeat);
		model.addAttribute("my_board_list", mapper.member_board(start, end));
	}

	@Override
	public void memberInfo(Model model, int num) {
		int pageLetter = 10; 
		int allCount = mapper.selectMemberCount(); 
		int repeat = allCount/pageLetter;  
		if(allCount % pageLetter != 0)
			repeat += 1;
		int end = num * pageLetter;
		int start = end + 1 - pageLetter;
		model.addAttribute("repeat", repeat);
		model.addAttribute("memberList", mapper.memberInfo(start, end));
		
	}
	
	public void manager_board(Model model, int num) {
		int pageLetter = 5; 
		int allCount = mapper.selectBoardCount(); 
		int repeat = allCount/pageLetter;  
		if(allCount % pageLetter != 0)
			repeat += 1;
		int end = num * pageLetter;
		int start = end + 1 - pageLetter;
		model.addAttribute("repeat", repeat);
		model.addAttribute("boardList", mapper.manager_board(start, end));
	}

	@Override
	public void manager_qna(Model model, int num) {
		int pageLetter = 10; 
		int allCount = mapper.selectQnaCount(); 
		int repeat = allCount/pageLetter;  
		if(allCount % pageLetter != 0)
			repeat += 1;
		int end = num * pageLetter;
		int start = end + 1 - pageLetter;
		model.addAttribute("repeat", repeat);
		model.addAttribute("qnaList", mapper.manager_qna(start, end));		
		
	}
	
	

	@Override
	public void member_leave_save(HttpServletRequest request) {
		MemberDTO dto = new MemberDTO();
		dto.setId(request.getParameter("id"));
		dto.setPwd(request.getParameter("pwd"));
		mapper.member_leave_save(dto);
	}

	@Override
	public void userInfo(String userid, Model model) {
		MemberDTO dto = mapper.getMember(userid);
		model.addAttribute("info", dto);
		
	}
	
	//============================ 박성수 시작 ===========================================
	
	
	@Override
	public String registHost(MemberDTO dto) {
		String message = "";
		int result = 0;
		if(dto.getUserSelect().equals("member")) {
			result = mapper.registMember(dto);
			return message;
		}
		result = mapper.registHost(dto);
		if(result == 1) {
			message = "회원가입 완료.";
		}
		return message;
	}
	
	@Override
	public String getHostInfo(String id) {
		MemberDTO dto = new MemberDTO();
		dto = mapper.getHostInfo(id);
		if(dto == null) {
			return "OK";
		}
		return "NO";
	}
	
	@Override
	public String checkEmail(String email) {
		MemberDTO dto = new MemberDTO();
		dto = mapper.checkEmail(email);
		if(dto == null) {
			return "OK";
		}
		return "NO";
	}
	
	@Override
	public int sendEmail(String email) {
		return cms.sendEmail(email); 
		
	}
	
	
	
	@Override
	public String getkakaoToken(String code, String tokenURL) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		
		MultiValueMap<String, String> params =
				new LinkedMultiValueMap<String, String>();
		params.add("grant_type", "authorization_code");		
		params.add("client_id", "50cc79dc82404d34d4da829c82e31cd2");		
		params.add("redirect_uri", "http://localhost:8080/root/member/kakaoCode");		
		params.add("code", code);
		
		HttpEntity<MultiValueMap<String, String>> entity =
				new HttpEntity<>(params, headers);
		
		ResponseEntity<String> response = 
				restTemplate.postForEntity(tokenURL, entity, String.class);
		
		return objectMapper.readTree(response.getBody())
					.get("access_token").asText();
		
	}
	
	@Override
	public int registKakaoUser(String token, String kakaoIdURL, HttpSession session) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		
		headers.add("Authorization", "Bearer "+token);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = 
					 restTemplate.exchange(kakaoIdURL, HttpMethod.GET, entity, String.class);
		
		JsonNode responseBody = objectMapper.readTree(response.getBody());
		String kakaoId = responseBody.get("id").asText();
		String kakaoLoginEmail = null;
		if(responseBody.get("kakao_account").get("email") != null) {
			kakaoLoginEmail = responseBody.get("kakao_account").get("email").asText();
		}
		KakaoLoginDTO kakaoLogindto = new KakaoLoginDTO(kakaoId, kakaoLoginEmail);
		KakaoLoginDTO kakaoUserCheck = mapper.kakaoUserCheck(kakaoId);
		
		int result = 0;
		if(kakaoUserCheck == null) {
			result = mapper.registKakaoUser(kakaoLogindto);
		}
		session.setAttribute("kakaoId", kakaoId);
		session.setAttribute("kakaoAccessToken", token);
		return result;
	}
	
	@Override
	public String kakaoLogout(String token, String kakaoLogouturl) {
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		
		headers.add("Content-type", "application/x-www-form-urlencoded");
		headers.add("Authorization", "Bearer "+token);
		
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = 
				restTemplate.postForEntity(kakaoLogouturl, entity, String.class);
		
		String kakaoLogoutId = "";
		try {
			kakaoLogoutId = objectMapper.readTree(response.getBody()).get("id").asText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return kakaoLogoutId;
	}
	
	@Override
	public String readyKakaoPay(
				String adminKey,
				String contentType,
				String kakaoPayReadyUrl,
				ItemDTO itemDTO,
				HttpSession session) {
		
		ObjectMapper objectMapper = new ObjectMapper();
		HttpHeaders headers = new HttpHeaders();
		RestTemplate restTemplate = new RestTemplate();
        
        headers.add("Authorization", "KakaoAK " + adminKey);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params =
				new LinkedMultiValueMap<String, String>();
        params.add("cid", "TC0ONETIME");
        params.add("partner_order_id", "mateWith_Partner");
        params.add("partner_user_id", "mateWith_User");
        params.add("item_name", itemDTO.getItem_name());
        params.add("quantity", Integer.toString(itemDTO.getQuantity()));
        params.add("total_amount", Integer.toString(itemDTO.getTotal_amount()));
        params.add("vat_amount", Integer.toString(itemDTO.getTotal_amount()/10));
        params.add("tax_free_amount", "0");
        params.add("approval_url", "http://localhost:8080/root/member/success");
        params.add("fail_url", "http://localhost:8080/root/member/fail");
        params.add("cancel_url", "http://localhost:8080/root/member/kakaoPayBtn");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = 
        		restTemplate.postForEntity(kakaoPayReadyUrl, entity, String.class);
        
        String kakaoPayRequestURL = ""; 
        String tid = "";
        String created_at = "";
		try {
			kakaoPayRequestURL = objectMapper.readTree(response.getBody())
								.get("next_redirect_pc_url").asText();
			tid = objectMapper.readTree(response.getBody())
					.get("tid").asText();
			
			session.setAttribute("tid", tid);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return kakaoPayRequestURL;
	}
	
	@Override
	public void kakaoPaymentApprove(String kakaoPaymentApproveUrl, String adminKey, String pg_token, HttpSession session) {
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		String tid = (String)session.getAttribute("tid");
		
		headers.add("Authorization", "KakaoAK " + adminKey);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		
		MultiValueMap<String, String> params =
				new LinkedMultiValueMap<String, String>();
        params.add("cid", "TC0ONETIME");
        params.add("tid", tid);
        params.add("partner_order_id", "mateWith_Partner");
        params.add("partner_user_id", "mateWith_User");
        params.add("pg_token", pg_token);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = 
        		restTemplate.postForEntity(kakaoPaymentApproveUrl, entity, String.class);
		try {
			JsonNode responseBody = objectMapper.readTree(response.getBody());
			String sid = "";
			String card_info = "";
			String item_code = "";
			String payload = "";
			
			if(responseBody.get("sid") != null) {
				sid = responseBody.get("sid").asText();
			}
			if(responseBody.get("card_info") != null) {
				card_info = responseBody.get("card_info").toString();
			}
			if(responseBody.get("item_code") != null) {
				item_code = responseBody.get("item_code").asText();
			}
			if(responseBody.get("payload") != null) {
				payload = responseBody.get("payload").asText();
			}
			
			String aid = responseBody.get("aid").asText();
			String cid = responseBody.get("cid").asText();
			String partner_order_id = responseBody.get("partner_order_id").asText();
			String partner_user_id = responseBody.get("partner_user_id").asText();
			String payment_method_type = responseBody.get("payment_method_type").asText();
			String amount = responseBody.get("amount").toString();
			String item_name = responseBody.get("item_name").asText();
			String quantity = responseBody.get("quantity").asText();
			String created_at = responseBody.get("created_at").asText();
			String approved_at = responseBody.get("approved_at").asText();
			
			KakaoPaymentApproveResultDTO kakaoPaymentApproveResultDTO =
					new KakaoPaymentApproveResultDTO(); 
			kakaoPaymentApproveResultDTO.setAid(aid);
			kakaoPaymentApproveResultDTO.setTid(tid);
			kakaoPaymentApproveResultDTO.setCid(cid);
			kakaoPaymentApproveResultDTO.setSid(sid);
			kakaoPaymentApproveResultDTO.setPartner_order_id(partner_order_id);
			kakaoPaymentApproveResultDTO.setPartner_user_id(partner_user_id);
			kakaoPaymentApproveResultDTO.setPayment_method_type(payment_method_type);
			kakaoPaymentApproveResultDTO.setAmount(amount);
			kakaoPaymentApproveResultDTO.setCard_info(card_info);
			kakaoPaymentApproveResultDTO.setItem_name(item_name);
			kakaoPaymentApproveResultDTO.setItem_code(item_code);
			kakaoPaymentApproveResultDTO.setQuantity(quantity);
			kakaoPaymentApproveResultDTO.setCreated_at(created_at);
			kakaoPaymentApproveResultDTO.setApproved_at(approved_at);
			kakaoPaymentApproveResultDTO.setPayload(payload);
			
			kakaoPayMapper.registKakaoPaymentApproveResult(kakaoPaymentApproveResultDTO);
			session.removeAttribute("tid");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 카카오페이 결제 승인 리스트
	@Override
	public void getkakaoPaymentApproveList(int num, HttpServletRequest request, Model model) { 
		// num = 현재 페이지
		int pageLetter = 5; // 한 페이지 당 글 목록수
		int allCount= kakaoPayMapper.selectKakaoPaymentApproveCount(); // 전체 글수
		int repeat = allCount/pageLetter; // 마지막 페이지 번호
		if(allCount % pageLetter != 0)
			repeat += 1;
		int end = num * pageLetter; // start ~ end -> 각 페이지에 불러올 글을 위한 쿼리용 숫자.
		int start = end +1 - pageLetter;
		
		// 페이징
		int totalPage = (allCount - 1)/pageLetter + 1;
		int block = 3;
		int startPage = (num - 1)/block*block + 1;
		int endPage = startPage + block - 1;
		if (endPage > totalPage) endPage = totalPage;
	
		model.addAttribute("repeat", repeat);
		model.addAttribute("kakaoPaymAppList", kakaoPayMapper.selectKakaoPaymentApproveList(start, end));
		model.addAttribute("endPage", endPage);
		model.addAttribute("startPage", startPage);
		model.addAttribute("block", block);
		model.addAttribute("totalPage", totalPage);
	}
	
	// 카카오페이 승인 상세 내역
	@Override
	public void selectKakaoPaymentOrderInfo(String kakaoPaymentOrderUrl, String adminKey,
									 	    Model model, HttpServletRequest request) {
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		
		headers.add("Authorization", "KakaoAK " + adminKey);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		
		MultiValueMap<String, String> params =
				new LinkedMultiValueMap<String, String>();
        params.add("cid", request.getParameter("cid"));
        params.add("tid", request.getParameter("tid"));
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = 
        		restTemplate.postForEntity(kakaoPaymentOrderUrl, entity, String.class);
		try {
			JsonNode responseBody = objectMapper.readTree(response.getBody());
			String selected_card_info = "";
			String item_code = "";
			String canceled_amount = "";
			String cancel_available_amount = "";
			String canceled_at = "";
			
			if(responseBody.get("selected_card_info") != null) {
				selected_card_info = responseBody.get("selected_card_info").toString();
			}
			if(responseBody.get("item_code") != null) {
				item_code = responseBody.get("item_code").asText();
			}
			if(responseBody.get("canceled_amount") != null) {
				canceled_amount = responseBody.get("canceled_amount").toString();
			}
			if(responseBody.get("cancel_available_amount") != null) {
				cancel_available_amount = responseBody.get("cancel_available_amount").toString();
			}
			if(responseBody.get("canceled_at") != null) {
				canceled_at = responseBody.get("canceled_at").asText();
			}
			
			String cid = responseBody.get("cid").asText();
			String tid = responseBody.get("tid").asText();
			String status = responseBody.get("status").asText();
			String partner_order_id = responseBody.get("partner_order_id").asText();
			String partner_user_id = responseBody.get("partner_user_id").asText();
			String payment_method_type = responseBody.get("payment_method_type").asText();
			String amount = responseBody.get("amount").toString();
			String item_name = responseBody.get("item_name").asText();
			String quantity = responseBody.get("quantity").asText();
			String created_at = responseBody.get("created_at").asText();
			String approved_at = responseBody.get("approved_at").asText();
			String payment_action_details = responseBody.get("payment_action_details").toString();
			
			KakaoPaymentOrderInfoDTO kakaoPaymentOrderInfoDTO =
					new KakaoPaymentOrderInfoDTO();
			kakaoPaymentOrderInfoDTO.setTid(tid);
			kakaoPaymentOrderInfoDTO.setCid(cid);
			kakaoPaymentOrderInfoDTO.setStatus(status);
			kakaoPaymentOrderInfoDTO.setPartner_order_id(partner_order_id);
			kakaoPaymentOrderInfoDTO.setPartner_user_id(partner_user_id);
			kakaoPaymentOrderInfoDTO.setPayment_method_type(payment_method_type);
			kakaoPaymentOrderInfoDTO.setAmount(amount);
			kakaoPaymentOrderInfoDTO.setSelected_card_info(selected_card_info);
			kakaoPaymentOrderInfoDTO.setItem_name(item_name);
			kakaoPaymentOrderInfoDTO.setItem_code(item_code);
			kakaoPaymentOrderInfoDTO.setQuantity(quantity);
			kakaoPaymentOrderInfoDTO.setCreated_at(created_at);
			kakaoPaymentOrderInfoDTO.setApproved_at(approved_at);
			kakaoPaymentOrderInfoDTO.setCanceled_at(canceled_at);
			kakaoPaymentOrderInfoDTO.setCanceled_amount(canceled_amount);
			kakaoPaymentOrderInfoDTO.setCancel_available_amount(cancel_available_amount);
			kakaoPaymentOrderInfoDTO.setPayment_action_details(payment_action_details);
			
			model.addAttribute("kakaoPaymOrderInfo", kakaoPaymentOrderInfoDTO);
			model.addAttribute("total", responseBody.get("amount").get("total").asText());
			model.addAttribute("tax_free", responseBody.get("amount").get("tax_free").asText());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// 카카오페이 결제 취소
	@Override
	public void kakaoPaymentCancel(String kakaoPaymentCancelUrl, String adminKey, String contentType,
			HttpServletRequest request) {
		ObjectMapper objectMapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		
		headers.add("Authorization", "KakaoAK " + adminKey);
		headers.add("Content-type", contentType);
		
		MultiValueMap<String, String> params =
				new LinkedMultiValueMap<String, String>();
        params.add("cid", request.getParameter("cid"));
        params.add("tid", request.getParameter("tid"));
        params.add("cancel_amount", request.getParameter("cancel_amount"));
        params.add("cancel_tax_free_amount", request.getParameter("cancel_tax_free_amount"));
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = 
        		restTemplate.postForEntity(kakaoPaymentCancelUrl, entity, String.class);
			
	}
	
	
	//============================ 박성수 끝 ===========================================
	
	
	//============================ 최윤희 ===========================================	
	 
	
	// 로그인 유저 체크
	@Override
	public int userCheck(HttpServletRequest request) {
		MemberDTO dto = mapper.userCheck(request.getParameter("id"));  // 입력받은 아이디값 전달

		if(dto != null) {  // dto에 값이 있으면
			if(request.getParameter("pwd").equals(dto.getPwd())) {  // 입력받은 비밀번호와 
				return 1; // 로그인 성공							// dto에서 찾아 저장된 비밀번호가 같은지 확인
			}
		}
		return 0; // 로그인 실패	
	}
	
	
	// 아이디 찾기
	@Override
	public int findUserId(HttpServletRequest request, Model model) {
		
		// 입력받은 이메일 전달
		MemberDTO dto = mapper.findUserId(request.getParameter("findUserEmail")); 
		
		// 입력받은 휴대폰 번호와 DB에서 찾은 휴대폰 번호가 같은지
		if(dto != null) {
			if(request.getParameter("findUserPhone").equals(dto.getPhone())) {  
				model.addAttribute("findUserId", dto);
				return 1;
			}
		}
		return 0;
	}
	
	
	// 비밀번호 찾기
	@Override
	public int findUserPwd(HttpServletRequest request, Model model) {
		
		// 입력받은 아이디 전달하여 dto에 회원정보 전부 저장
		MemberDTO dto = mapper.findUserPwd(request.getParameter("findUserId"));
		
		if(dto != null) {
			model.addAttribute("findUserPwd", dto);
			return 1;
		} 
		return 0;
	}
	
	
	// 비밀번호 찾기 메일 인증
	@Override
	public int sendFindPwdEmailCode(String email) {
		return cms.sendFindPwdEmailCode(email);
	}

	
	// 비밀번호 수정
	@Override
	public void userUpdatePwd(MemberDTO dto) {
		mapper.userUpdatePwd(dto); 
	}

	
	//============================ 최윤희 끝 ===========================================
	
	
	
}











