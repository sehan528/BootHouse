package likelion.eight.domain.user.converter;

import likelion.eight.domain.user.model.User;
import likelion.eight.user.UserEntity;
import likelion.eight.user.enums.GenderType;
import likelion.eight.user.enums.RoleType;
import likelion.eight.userinterest.UserInterestEntity;

public class UserConverter {

    public static UserEntity toEntity(User user){
        return UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .nickName(user.getNickName())
                .phoneNumber(user.getPhoneNumber())
                .genderType(user.getGenderType())
                .roleType(user.getRoleType())
                .image(user.getImage())
                .build();
    }
}