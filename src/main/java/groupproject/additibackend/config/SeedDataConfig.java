package groupproject.additibackend.config;

import groupproject.additibackend.model.*;
import groupproject.additibackend.repository.CategoryRepository;
import groupproject.additibackend.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seed(CategoryRepository categoryRepo, ProductRepository productRepo) {
        return args -> {

            // avoid duplicate seed
            if (productRepo.count() > 0) return;

            // 1) category
            Category cat = new Category();
            cat.setName("Shoes");
            cat.setSlug("shoes");
            cat.setDescription("Demo category");
            cat.setIsActive(true);
            cat = categoryRepo.save(cat);

            // 2) product
            Product p = new Product();
            p.setName("Nike Air Max");
            p.setDescription("Demo product with variants");
            p.setPrice(new BigDecimal("120.00"));
            p.setBrand("NIKE");
            p.setIsActive(true);
            p.setCategory(cat);

            // 3) variant 1
            ProductVariant v1 = new ProductVariant();
            v1.setSize("M");
            v1.setColor("BLACK");
            v1.setSku("NIKEAIRMAX-BLK-M");
            v1.setStockQuantity(10);
            v1.setPriceAdjustment(BigDecimal.ZERO);
            v1.setIsAvailable(true);

            ProductImage img1 = new ProductImage();
            img1.setImageUrl("https://your-r2-domain/products/nike-air-max/black-m-1.jpg");
            img1.setImageKey("products/nike-air-max/black-m-1.jpg");
            v1.addImage(img1);

            // 4) variant 2
            ProductVariant v2 = new ProductVariant();
            v2.setSize("L");
            v2.setColor("WHITE");
            v2.setSku("NIKEAIRMAX-WHT-L");
            v2.setStockQuantity(5);
            v2.setPriceAdjustment(new BigDecimal("5.00"));
            v2.setIsAvailable(true);

            ProductImage img2 = new ProductImage();
            img2.setImageUrl("https://your-r2-domain/products/nike-air-max/white-l-1.jpg");
            img2.setImageKey("products/nike-air-max/white-l-1.jpg");
            v2.addImage(img2);

            // attach variants to product (IMPORTANT)
            p.addVariant(v1);
            p.addVariant(v2);

            // save once (cascade saves variants + images)
            productRepo.save(p);
        };
    }
}
