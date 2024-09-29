package com.example.samuraitravel.form;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ReviewAddForm {    
    @NotNull(message = "評価を選択してください")
    private String reviewRank;
    
    @NotBlank(message = "コメントを入力してください")
    private String reviewContent;
    
}
