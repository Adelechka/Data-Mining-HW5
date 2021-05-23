package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pair {
    private Integer first;
    private Integer second;

    public Integer countHash1(Integer k) {
        return (first + second) % k;
    }

    public Integer countHash2(Integer k) {
        return (first + 2 * second) % k;
    }
}
