package gift.controller;

import gift.entity.Member;
import gift.entity.Product;
import gift.entity.Wish;
import gift.exception.CustomException;
import gift.repository.MemberRepository;
import gift.repository.ProductRepository;
import gift.service.WishService;
import gift.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/wishes")
public class WishController {
    private final WishService wishService;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public WishController(WishService wishService, MemberRepository memberRepository, ProductRepository productRepository, JwtUtil jwtUtil) {
        this.wishService = wishService;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<Wish> addWish(@RequestBody Wish wish, HttpServletRequest request) {
        String email = jwtUtil.getEmailFromRequest(request);
        if(email == null) {
            throw new CustomException.InvalidCredentialsException("Invalid email");
        }
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomException.EntityNotFoundException("Member not found"));
        Product product = productRepository.findById(wish.getProductId()).orElseThrow(() -> new CustomException.EntityNotFoundException("Product not found"));

        Wish wishBuilder = new Wish.Builder()
                .member(member)
                .product(product)
                .build();
        Wish savedWish = wishService.save(wishBuilder);
        return ResponseEntity.ok(savedWish);
    }

    @GetMapping
    public ResponseEntity<List<Wish>> getWishes(HttpServletRequest request) {
        String email = jwtUtil.getEmailFromRequest(request);
        if (email == null) {
            throw new CustomException.InvalidCredentialsException("Invalid email");
        }
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomException.EntityNotFoundException("Member not found"));
        List<Wish> wishes = wishService.findByMember(member);
        return ResponseEntity.ok(wishes);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWish(@PathVariable Long id, HttpServletRequest request) {
        String email = jwtUtil.getEmailFromRequest(request);
        if (email == null) {
            throw new CustomException.InvalidCredentialsException("Invalid email");
        }
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new CustomException.EntityNotFoundException("Member not found"));
        wishService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}