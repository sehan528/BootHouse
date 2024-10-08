package likelion.eight.domain.review.service;

import likelion.eight.common.domain.exception.ResourceNotFoundException;
import likelion.eight.common.service.port.ClockHolder;
import likelion.eight.course.CourseEntity;
import likelion.eight.domain.course.controller.CourseController;
import likelion.eight.domain.course.converter.CourseConverter;
import likelion.eight.domain.course.service.port.CourseRepository;
import likelion.eight.domain.review.controller.model.*;
import likelion.eight.domain.review.converter.ReviewConverter;
import likelion.eight.domain.review.model.Review;
import likelion.eight.domain.review.service.port.ReviewRepository;
import likelion.eight.domain.user.controller.model.LoginUser;
import likelion.eight.domain.user.converter.UserConverter;
import likelion.eight.domain.user.model.User;
import likelion.eight.domain.user.service.UserService;
import likelion.eight.domain.user.service.port.UserRepository;
import likelion.eight.likeReview.LikeReviewEntity;
import likelion.eight.likeReview.LikeReviewJpaRepository;
import likelion.eight.review.ReviewEntity;
import likelion.eight.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ClockHolder clockHolder;
    private final LikeReviewJpaRepository likeReviewJpaRepository;

    public Page<ReviewAll> getReviewAll(Pageable pageable) {
        return reviewRepository.findAll(pageable)
                .map(review -> {
                    String courseName = getCourseName(review.getCourseEntity().getId());
                    String author = getAuthor(review.getUserEntity().getId());
                    return new ReviewAll(ReviewConverter.toDto(review), courseName, author);
                });
    }

    public Page<ReviewAll> searchReviews(ReviewSearchCondition searchCondition, Pageable pageable) {
        return reviewRepository.searchByKeyword(searchCondition, pageable)
                .map(review -> {
                    String courseName = getCourseName(review.getCourseEntity().getId());
                    String author = getAuthor(review.getUserEntity().getId());
                    return new ReviewAll(ReviewConverter.toDto(review), courseName, author);
                });
    }

    public Page<ReviewAll> sortReviews(ReviewSortCondition sortCondition, Pageable pageable) {
        return reviewRepository.sortByCondition(sortCondition, pageable)
                .map(review -> {
                    String courseName = getCourseName(review.getCourseEntity().getId());
                    String author = getAuthor(review.getUserEntity().getId());
                    return new ReviewAll(ReviewConverter.toDto(review), courseName, author);
                });
    }


    public String getAuthor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found : " + userId));

        return user.getName();
    }

    public String getCourseName(Long courseId) {
        CourseEntity courseEntity = courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course Not Found : " + courseId));

        return courseEntity.getName();

    }


    @Transactional(readOnly = true)
    public Review findReviewById(Long id) {
        ReviewEntity reviewEntity = getReviewEntity(id);
       return ReviewConverter.toDto(reviewEntity);
    }

    @Transactional(readOnly = true)
    public Optional<Review> findPreviousReview(Long reviewId) {
        return reviewRepository.findPreviousReview(reviewId)
                .map(ReviewConverter::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<Review> findNextReview(Long reviewId) {
        return reviewRepository.findNextReview(reviewId)
                .map(ReviewConverter::toDto);
    }


    @Transactional
    public void saveReview(ReviewCreateRequest reviewCreateRequest, Long userId, Long courseId) {

        CourseEntity courseEntity = getCourseEntity(courseId);
        UserEntity userEntity = getUserEntity(userId);

        ReviewEntity reviewEntity = ReviewConverter.toReviewEntity(reviewCreateRequest, courseEntity, userEntity, clockHolder);

        reviewRepository.save(reviewEntity, courseEntity, userEntity);

    }
    @Transactional
    public void updateReview(Long id, ReviewUpdateRequest reviewUpdateRequest) {
        ReviewEntity reviewEntity = getReviewEntity(id);
        Review review = ReviewConverter.toDto(reviewEntity);

        CourseEntity courseEntity = getCourseEntity(review.getCourseId());
        UserEntity userEntity = getUserEntity(review.getUserId());

        review.update(reviewUpdateRequest, clockHolder);

        reviewRepository.save(review, courseEntity,userEntity);

    }

    @Transactional
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByUserIdAndCourseId(Long userId, Long courseId) {
        return reviewRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Transactional(readOnly = true)
    public Review findReviewByCourseIdAndUserId(Long courseId, Long userId) {
        return reviewRepository.findByCourseIdAndUserId(courseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review Not Found"));
    }

    @Transactional
    public void incrementViewCount(Long reviewId) {

        ReviewEntity reviewEntity = getReviewEntity(reviewId);
        Review review = ReviewConverter.toDto(reviewEntity);

        CourseEntity courseEntity = getCourseEntity(review.getCourseId());
        UserEntity userEntity = getUserEntity(review.getUserId());

        reviewEntity.incrementViewCount();

        reviewRepository.save(ReviewConverter.toDto(reviewEntity), courseEntity, userEntity);


    }


    //좋아요가 되어있는지 확인 -> 로딩시 좋아요되어있으면 색깔로 표시하기위해
    public Boolean isLiked(Long reviewId, LoginUser loginUser){
        ReviewEntity review = getReviewEntity(reviewId);
        UserEntity user = getUserEntity(loginUser.getId());
        Optional<LikeReviewEntity> isLiked = likeReviewJpaRepository.findByUserEntityAndReviewEntity(user, review);
        if(isLiked.isPresent()){
            return true;
        }
        return false;

    }

    public List<Review> findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found "));

        List<ReviewEntity> reviewEntities = reviewRepository.findByUserEntityByUser(user);

        return reviewEntities.stream().map(ReviewConverter::toDto)
                .collect(Collectors.toList());
    }

    // 중복 함수 빼 놓음
    private ReviewEntity getReviewEntity(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review Not Found : " + reviewId));
    }

    private CourseEntity getCourseEntity(Long courseId) {
        return courseRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    }

    private UserEntity getUserEntity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return UserConverter.toEntity(user);
    }

}
