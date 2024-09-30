package com.example.samuraitravel.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Likes;
import com.example.samuraitravel.entity.User;


@Repository
public interface LikeRepository extends JpaRepository<Likes, Integer> {
	public  Likes findByHouseAndUser(House house, User user);
	public Page<Likes> findByUser(User user, Pageable pageable);

}