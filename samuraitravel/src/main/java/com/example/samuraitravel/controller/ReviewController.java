package com.example.samuraitravel.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.HouseEditForm;
import com.example.samuraitravel.form.ReviewAddForm;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl; 


@RequestMapping
@Controller
public class ReviewController {

	private final ReviewRepository reviewRepository;
	private final HouseRepository houseRepository;
	public ReviewController(ReviewRepository reviewRepository,HouseRepository houseRepository) {
		this.reviewRepository = reviewRepository;
		this.houseRepository = houseRepository;
		}
	
	@GetMapping("/houses/{id}/evaluation")
    public String index(@PathVariable("id") Integer id,
    		            @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Direction.DESC) Pageable pageable,
    		            Model model) {
        House house =houseRepository.findById(id).orElse(null);
           if(house == null) { model.addAttribute("errorMessage", "House not found");
           }else {
        
        Page<Review> reviewPage = reviewRepository.findByHouseOrderByUpdatedAtDesc(house, pageable);
        
        if (reviewPage != null) {
            System.out.println("Total pages: " + reviewPage.getTotalPages());
            System.out.println("Current page: " + reviewPage.getNumber());
        }
        
        model.addAttribute("house",house);
        model.addAttribute("reviewPage", reviewPage);         
        model.addAttribute("id",id);
                }
        return "evaluation/index";
    }
    
	
	
    @GetMapping("/houses/{id}/evaluation/review/input")
    public String input(@PathVariable(name="id") Integer id,Model model,HouseEditForm houseEditForm) {

    	
    	List<String> ratings = Arrays.asList("★★★★★", "★★★★☆", "★★★☆☆", "★★☆☆☆", "★☆☆☆☆"); // 評価の選択肢
        System.out.println("House ID: " + id);  //エラー確認用
        House house = houseRepository.findById(id).orElse(null);
        if(house!=null) {
        model.addAttribute("house",house);
        model.addAttribute("id",id);
        model.addAttribute("ratings", ratings);
        model.addAttribute("reviewAddForm", new ReviewAddForm()); // フォームの初期化
        }
        return "evaluation/review"; // フォームのテンプレート名
    }
    
    @PostMapping("/houses/{id}/evaluation/review/confirm")
    public String submitReview(@PathVariable(name="id") Integer id,@ModelAttribute @Validated ReviewAddForm reviewAddForm,BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        House house = houseRepository.findById(id).orElse(null);
    	Review review = new Review();
    	review.setReviewRank(reviewAddForm.getReviewRank());
    	review.setReviewContent(reviewAddForm.getReviewContent());
    	review.setUser(userDetails.getUser());
    	review.setHouse(house);
    	
    	if(bindingResult.hasErrors()) {
    		model.addAttribute("reviewAddForm",reviewAddForm);
    		model.addAttribute("errorMessage","入力内容に不備があります");
    		return "evaluation/review";
    	}
    	
    	if(house !=null) {
    	reviewRepository.save(review);
    	
    	redirectAttributes.addAttribute("reviewAdded", true);
    	}
    	return "redirect:/houses/" + id;

        
    
    }
    
    @PostMapping("/houses/{id}/evaluation/review/delete")
    public String deleteReview(@PathVariable(name="id")Integer id,
    		      RedirectAttributes redirectAttributes,
    		      Model model,
    		      @RequestParam("reviewId") Integer reviewId,
    		      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    	
    	reviewRepository.deleteById(reviewId);
    	redirectAttributes.addFlashAttribute("successMessage","レビューが削除されました。");
    	redirectAttributes.addAttribute("reviewDeleted", true);
    	
    	return"redirect:/houses/"+id;
    }
    
    //以下編集用　更新中
    
    @GetMapping("/houses/{id}/evaluation/review/edit")
    public String edit(@PathVariable(name="id") Integer id,
    		           Model model,
    		           HouseEditForm houseEditForm,
    		           @AuthenticationPrincipal UserDetailsImpl userDetails) {

    	
    	List<String> ratings = Arrays.asList("★★★★★", "★★★★☆", "★★★☆☆", "★★☆☆☆", "★☆☆☆☆"); // 評価の選択肢
        System.out.println("House ID: " + id);  //エラー確認用
        
        //民宿情報を取得
        House house = houseRepository.findById(id).orElse(null);
        if(house!=null) {
        model.addAttribute("house",house);
        model.addAttribute("id",id);
        model.addAttribute("ratings", ratings);
        ReviewEditForm reviewEditForm = new ReviewEditForm();
        
        //ログインユーザーのID取得
        User user = userDetails.getUser();
        
        //既存のレビュー獲得
        Review existingReview = reviewRepository.findByUserAndHouse(user,house);
        
        if (existingReview != null) {
            // 既存のレビューがあれば、フォームにデフォルト表示するためにモデルに追加
            reviewEditForm.setReviewRank(existingReview.getReviewRank());
            reviewEditForm.setReviewContent(existingReview.getReviewContent());
        	model.addAttribute("reviewEditForm", reviewEditForm);
        }
        }
        return "evaluation/edit"; // フォームのテンプレート名
    }
    
    @PostMapping("/houses/{id}/evaluation/review/edit/confirm")
    public String editConfirm(@PathVariable(name="id") Integer id,
    		  					@ModelAttribute @Validated ReviewEditForm reviewEditForm,
    		  					BindingResult bindingResult,
    		  					RedirectAttributes redirectAttributes, 
    		  					Model model,
    		  					@AuthenticationPrincipal UserDetailsImpl userDetails) {
        
    	//民宿情報を取得
    	House house = houseRepository.findById(id).orElse(null);
    	//ユーザー情報を取得
    	User user = userDetails.getUser();
    	
    	//既存のレビューを取得
    	Review existingReview = reviewRepository.findByUserAndHouse(user,house);
    	
    	existingReview.setReviewRank(reviewEditForm.getReviewRank());
    	existingReview.setReviewContent(reviewEditForm.getReviewContent());
    	
    	
    	if(bindingResult.hasErrors()) {
    		model.addAttribute("reviewAddForm",reviewEditForm);
    		model.addAttribute("errorMessage","入力内容に不備があります");
    		return "evaluation/edit";
    	}
    	
    	if(house !=null) {
    		
    	reviewRepository.save(existingReview);
    	
    	redirectAttributes.addAttribute("reviewEdited", true);
    	}
    	return "redirect:/houses/" + id;

        
    
    }
    
    
}