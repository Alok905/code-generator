package com.alok.projects.lovable_clone.mapper;

import com.alok.projects.lovable_clone.dto.member.MemberResponse;
import com.alok.projects.lovable_clone.entity.ProjectMember;
import com.alok.projects.lovable_clone.entity.User;
import com.alok.projects.lovable_clone.enums.ProjectRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "projectRole", constant = "OWNER")
    MemberResponse toMemberResponseFromOwner(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    MemberResponse toMemberResponseFromMember(ProjectMember member);
}
