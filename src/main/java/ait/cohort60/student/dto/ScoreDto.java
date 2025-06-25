package ait.cohort60.student.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ScoreDto {
    private String examName;
    private Integer score;

    public ScoreDto(String math, int i) {
    }
}
