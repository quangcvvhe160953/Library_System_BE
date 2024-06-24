package swp391.learning.domain.dto.request.admin.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateCategoryRequest {
    @NotNull
    private int updatedById;
    private int parentId;
    @NotBlank
    private String name;
    private int categoryId;

}
