package com.example.samuraitravel.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Likes;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.LikeRepository;
import com.example.samuraitravel.security.UserDetailsImpl;

@Controller
@RequestMapping("/likesAll")
public class LikesController {


    private final LikeRepository likeRepository;
    private final HouseRepository houseRepository;

    public LikesController(LikeRepository likeRepository, HouseRepository houseRepository) {
        this.likeRepository = likeRepository;
        this.houseRepository = houseRepository; 
    }     
  
    @GetMapping
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetails,
    					@PageableDefault(page = 0, size = 5, sort = "id", direction = Direction.ASC) Pageable pageable,
                        Model model) 
    {
    	User user = userDetails.getUser();
    	System.out.println("User:" + user);
    	Page<Likes> likesPage  = likeRepository.findByUser(user,pageable);
    	System.out.println("likesPage:" + likesPage);          
        
        List<House> houses = likesPage.getContent().stream()
    	  .map(Likes::getHouse)
    	  .collect(Collectors.toList());
        		
        model.addAttribute("likesPage", likesPage);
        model.addAttribute("houses",houses);
        
        return "likes/likes";
    }
  
 
 
}
