package com.example.demo.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.member.domain.RefBoardDTO;
import com.example.demo.member.domain.RefBoardReply;
import com.example.demo.member.repository.RefBoardRepository;
import com.example.demo.util.PageDTO;
import com.example.demo.util.PagingCriteria;

@CrossOrigin("*")
@Controller
@RequestMapping("/ref")
public class RefcheckBoardController {

	@Autowired
	RefBoardRepository mapper;
	
	@GetMapping
	public String getBoard(PagingCriteria cri ,Model model, @RequestParam(value ="rel",required = false) String rel) {
		
		
		if(rel=="") {

			rel=null;
		}


		cri.setRel(rel);
		
		
				
		PageDTO pager = new PageDTO(cri, mapper.getTotal(cri));
		
		System.out.println(pager.toString());
		
		List<RefBoardDTO> lists = mapper.getListsWithPage(cri);
		
		model.addAttribute("list",lists);
		model.addAttribute("pager",pager);
		
		
		return "/refBoard/refBoard";
	}
	
	@GetMapping("/register")
	public String getForm(Model model) {
		SecurityContext ctx = SecurityContextHolder.getContext();
		String userName = ctx.getAuthentication().getName();
		
		System.out.println(userName);
		
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
		String localdate = formater.format(new Date());
		System.out.println(localdate);
		
		model.addAttribute("userName",userName);
		model.addAttribute("date",localdate);

		return "/refBoard/registerForm";

	}
	@PostMapping("/register")
	public String register(@ModelAttribute  RefBoardDTO vo) {
							//bno, registerDate 오라클에서 처리할 예정.
			
		System.out.println(vo.toString());

		mapper.insert(vo);

		
		
		return "redirect:/ref"; //리다이렉트 수정해주기
	}
	
	
	
	@GetMapping("/detail")
    public String getAList(@RequestParam("bno") Long bno, Model model, String pageNum, String rel)
	{
		
		HashMap<String, String> searchInfo = new HashMap<>();
		searchInfo.put("pageNum", pageNum);
		searchInfo.put("rel", rel);
		
		System.out.println(searchInfo);
		
		RefBoardDTO dto = mapper.getAList(bno);
		model.addAttribute("info",dto);
		model.addAttribute("page", searchInfo);
	
		return "/refBoard/tempDetail";
	}
	
	
	@GetMapping("/edit")
	public String editForm(@RequestParam("bno")Long bno, Model model) {
		RefBoardDTO dto = mapper.getAList(bno);
		model.addAttribute("info",dto);
		
		return "/refBoard/refEdit";
	}
	
	@PostMapping("/edit")
	public String edit(RefBoardDTO dto) {
		
		mapper.edit(dto);
		

		return "redirect:/ref";
	}
	@GetMapping("/remove")
	public String remove(@RequestParam("bno")Long bno, Model model) {
		int result = mapper.remove(mapper.getAList(bno).getBno());
		
		if(result>0) {
			model.addAttribute("result","게시물이 삭제되었습니다");
		}else {
			model.addAttribute("result","게시물이 삭제되지 않았습니다. 다시 시도해주세요");

		}
		return "redirect:/ref";

	}
	
	
	////////////////////////대글 관련
	
	@ResponseBody
	@PostMapping("/reply")
	public ResponseEntity<RefBoardReply> getReply(@RequestBody RefBoardReply vo) {

		System.out.println(vo.toString());
		
		try {
			
		if(vo.getRoot()==null) {
			System.out.println("루트X");
			mapper.insertReply(vo);
			System.out.println(mapper.getaReply(vo.getRno()));

		}else {
			System.out.println("루트 존재");

			mapper.insertReplyWithRoot(vo);
			System.out.println(mapper.getaReply(vo.getRno()));

		}
			
		System.out.println("저장 성공");
		return new ResponseEntity<RefBoardReply>(vo,HttpStatus.OK);		}
		
		catch(Exception e){
			System.out.println("실패");
			e.printStackTrace();
			return new ResponseEntity<RefBoardReply>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	
	
	@ResponseBody
	@GetMapping("/reply")
	public List<RefBoardReply> getReplyList(@RequestParam("bno")Long bno){
		System.out.println(bno+"의 댓글이 요청이 옴");
		System.out.println(mapper.getaReply(38L).toString());
		List<RefBoardReply>list = mapper.getReply(bno);
		
		list.forEach(a->System.out.println(a.toString()));
		
		return list;
	}
	
	
}
