package xyz.yaungyue.secondhand;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.yaungyue.secondhand.model.dto.request.CategoryCreateRequest;
import xyz.yaungyue.secondhand.model.dto.request.CategoryUpdateRequest;
import xyz.yaungyue.secondhand.model.dto.response.CategoryVO;
import xyz.yaungyue.secondhand.model.dto.response.CategoryTreeVO;
import xyz.yaungyue.secondhand.model.entity.Category;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("商品分类DTO和实体类单元测试")
public class CategoryModelTest {

    @Test
    @DisplayName("测试CategoryCreateRequest构造和访问")
    void testCategoryCreateRequest() {
        CategoryCreateRequest request = new CategoryCreateRequest(1L, "电子产品", "icon-url", 1);
        
        assertEquals(1L, request.parentId());
        assertEquals("电子产品", request.name());
        assertEquals("icon-url", request.icon());
        assertEquals(Integer.valueOf(1), request.sort());
    }

    @Test
    @DisplayName("测试CategoryUpdateRequest构造和访问")
    void testCategoryUpdateRequest() {
        CategoryUpdateRequest request = new CategoryUpdateRequest(1L, 2L, "数码产品", "new-icon", 2);
        
        assertEquals(1L, request.id());
        assertEquals(2L, request.parentId());
        assertEquals("数码产品", request.name());
        assertEquals("new-icon", request.icon());
        assertEquals(Integer.valueOf(2), request.sort());
    }

    @Test
    @DisplayName("测试CategoryVO构造和访问")
    void testCategoryVO() {
        LocalDateTime now = LocalDateTime.now();
        CategoryVO vo = new CategoryVO(1L, 2L, "电子产品", "icon-url", 1, now);
        
        assertEquals(1L, vo.id());
        assertEquals(2L, vo.parentId());
        assertEquals("电子产品", vo.name());
        assertEquals("icon-url", vo.icon());
        assertEquals(Integer.valueOf(1), vo.sort());
        assertEquals(now, vo.createTime());
    }

    @Test
    @DisplayName("测试CategoryTreeVO构造和访问")
    void testCategoryTreeVO() {
        LocalDateTime now = LocalDateTime.now();
        CategoryTreeVO child = new CategoryTreeVO(2L, 1L, "手机", "phone-icon", 1, now, Collections.emptyList());
        CategoryTreeVO parent = new CategoryTreeVO(1L, null, "电子产品", "icon-url", 1, now, Collections.singletonList(child));
        
        assertEquals(1L, parent.id());
        assertNull(parent.parentId());
        assertEquals("电子产品", parent.name());
        assertEquals(1, parent.children().size());
        assertEquals("手机", parent.children().get(0).name());
    }

    @Test
    @DisplayName("测试Category实体类构造和访问")
    void testCategoryEntity() {
        Category category = new Category();
        category.setId(1L);
        category.setParent_id(2L);
        category.setName("电子产品");
        category.setIcon("icon-url");
        category.setSort(1);
        category.setCreate_time(LocalDateTime.now());
        
        assertEquals(1L, category.getId());
        assertEquals(2L, category.getParent_id());
        assertEquals("电子产品", category.getName());
        assertEquals("icon-url", category.getIcon());
        assertEquals(Integer.valueOf(1), category.getSort());
        assertNotNull(category.getCreate_time());
    }

    @Test
    @DisplayName("测试Category实体类equals和hashCode")
    void testCategoryEqualsAndHashCode() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("电子产品");
        category1.setSort(1);
        
        Category category2 = new Category();
        category2.setId(1L);
        category2.setName("电子产品");
        category2.setSort(1);
        
        Category category3 = new Category();
        category3.setId(2L);
        category3.setName("服装");
        category3.setSort(2);
        
        assertEquals(category1, category2);
        assertNotEquals(category1, category3);
        assertEquals(category1.hashCode(), category2.hashCode());
        assertNotEquals(category1.hashCode(), category3.hashCode());
    }

    @Test
    @DisplayName("测试Category实体类toString")
    void testCategoryToString() {
        Category category = new Category();
        category.setId(1L);
        category.setName("电子产品");
        category.setSort(1);
        
        String toString = category.toString();
        assertTrue(toString.contains("Category"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name=电子产品"));
    }
}