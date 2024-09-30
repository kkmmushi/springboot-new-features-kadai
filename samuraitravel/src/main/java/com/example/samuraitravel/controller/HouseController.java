package com.example.samuraitravel.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Likes;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.LikeRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.repository.UserRepository;
import com.example.samuraitravel.security.UserDetailsImpl;


@Controller
@RequestMapping("/houses")
public class HouseController {
    private final HouseRepository houseRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    

    public HouseController(HouseRepository houseRepository , ReviewRepository reviewRepository, UserRepository userRepository,LikeRepository likeRepository) {
        this.houseRepository = houseRepository; 
        this.reviewRepository = reviewRepository;
        this.userRepository =userRepository;
        this.likeRepository = likeRepository;
    }     
  
    @GetMapping
    public String index(@RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "area", required = false) String area,
                        @RequestParam(name = "price", required = false) Integer price,  
                        @RequestParam(name = "order", required = false) String order,
                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                        Model model) 
    {
        Page<House> housePage;
                
        if (keyword != null && !keyword.isEmpty()) {            
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findByNameLikeOrAddressLikeOrderByPriceAsc("%" + keyword + "%", "%" + keyword + "%", pageable);
            } else {
                housePage = houseRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + keyword + "%", "%" + keyword + "%", pageable);
            }            
        } else if (area != null && !area.isEmpty()) {            
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findByAddressLikeOrderByPriceAsc("%" + area + "%", pageable);
            } else {
                housePage = houseRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
            }            
        } else if (price != null) {            
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
            } else {
                housePage = houseRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
            }            
        } else {            
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findAllByOrderByPriceAsc(pageable);
            } else {
                housePage = houseRepository.findAllByOrderByCreatedAtDesc(pageable);   
            }            
        }                
        
        model.addAttribute("housePage", housePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("area", area);
        model.addAttribute("price", price); 
        model.addAttribute("order", order);
        
        return "houses/index";
    }
  
    
    @GetMapping ("/{id}/deleteLike") 
    public String deleteLike(@PathVariable(name = "id") Integer id, 
			@AuthenticationPrincipal UserDetailsImpl userDetails,
 			Model model) {
    		House house = houseRepository.getReferenceById(id);
    		User user = userDetails.getUser();
    	    Likes likes  = likeRepository.findByHouseAndUser(house,user);
  
    		model.addAttribute("addLike",true);
    		model.addAttribute("house" ,house);
    		likeRepository.delete(likes);

    		return "houses/show";
    	}
 
    @GetMapping ("/{id}/addLike") 
    public String addLike(@PathVariable(name = "id") Integer id, 
			@AuthenticationPrincipal UserDetailsImpl userDetails,
 			Model model) {
    		House house = houseRepository.getReferenceById(id);
    		User user = userDetails.getUser();

    		model.addAttribute("deleteLike",true);
    		Likes newLike =new Likes();
    		newLike.setHouse(house);
    		newLike.setUser(user);
    		likeRepository.save(newLike);

    		model.addAttribute("house",house);
    		
    		return "houses/show";
    	}
    
    @GetMapping("/{id}")
    public String show(@PathVariable(name = "id") Integer id, 
    					@AuthenticationPrincipal UserDetailsImpl userDetails,
    		 			Model model) {
        House house = houseRepository.getReferenceById(id);
        
        
        
        List<Review> reviews = reviewRepository.findTop6ByHouseOrderByUpdatedAtDesc(house);
        List<Review> reviewsAll = reviewRepository.findByHouseOrderByUpdatedAtAsc(house);   
  
       
   
      if(userDetails != null) {  

          User user = userDetails.getUser();
          Likes likes  = likeRepository.findByHouseAndUser(house,user);
        
        //お気に入り情報がない場合
        if(likes==null){
         model.addAttribute("addLike",true);

        }else {
        	model.addAttribute("deleteLike",true);
        }
         
      }
        // レビューがない場合のメッセージ
        if (reviews.isEmpty()) {
            model.addAttribute("noReviewsMessage", "まだレビューがありません。");
        } else {
        
        //ログインユーザーのレビューを先頭に配置する
         Review userReview = null;
          if(userDetails != null) {
        	  User user = userDetails.getUser();
        	  
        	  userReview = reviewRepository.findByUserAndHouse(user,house);
          }
          if(userReview != null) {                    
        	    if (reviews.contains(userReview)) {
        	        reviews.remove(userReview);
        	    }
        	  reviews.add(0,userReview);
              model.addAttribute("userReview",userReview);
          }
          
          // 最終的にレビューは6件に制限する
          if (reviews.size() > 6) {
              reviews = reviews.subList(0, 6);
          }
         
          
          if(reviewsAll.size()>6) {
          
          model.addAttribute("allReviewsShowMessage","すべてのレビューを見る");
          }
          }

        model.addAttribute("id",id);
        model.addAttribute("house", house);  
        model.addAttribute("reviews",reviews);
        model.addAttribute("reservationInputForm", new ReservationInputForm());
        
        return "houses/show";
    }    
}
