package com.alok.projects.lovable_clone.dto.auth;

// record is a special type of class that has the fields "private and final" by default
// you can write getters. but not setters
// requires all args constructor (that is present by default)

// it is (token + UserProfileResponse)  == used for authentication via login or signup
public record AuthResponse(String token, UserProfileResponse user) {

}
