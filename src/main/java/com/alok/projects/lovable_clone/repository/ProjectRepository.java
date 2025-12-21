package com.alok.projects.lovable_clone.repository;

import com.alok.projects.lovable_clone.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {


    @Query("""
            SELECT p FROM Project p
            WHERE p.deletedAt IS NULL
            AND p.owner.id = :userId
            ORDER BY p.updatedAt desc
            """
    )
    List<Project> findAllAccessibleByUser(@Param("userId") Long userId);
//
//    // It will work only if the user is owner; not for user if present in members
//    @Query("""
//            SELECT p from Project p
//            WHERE p.deletedAt is null
//            AND p.id = :projectIf
//            AND p.owner.id = :userId
//            """)

//    //  userid can be project.owner.id or userid should be in project.projectMembers list
//    // left join because if we do inner join, then the owner will not be gotten if there is no member of the project is there.
//    // means inner join only works when project.id = projectMember.projectId; but if there is no member then there won't be any row inside projectMember which will be containing current project's id
//    // below we are trying to join 2 sql kind of tables, so we need to write the join condition; but it is not recommended for ORM
//    @Query("""
//            SELECT p from Project p
//            LEFT JOIN ProjectMember pm
//            ON p.id = pm.project.id
//            WHERE p.id = :projectId
//            AND (
//                p.owner.id = :userId
//                OR pm.user.id = :userId
//            )
//            """)

    // we only need the project if the owner is user; not member (will write the method for member in future)
//    @Query("""
//            SELECT p FROM Project p
//            LEFT JOIN p.owner AS po
//            WHERE p.id = :projectId
//            AND p.deletedAt = NULL
//            AND p.owner.id = userId
//            """)
    @Query("""
            SELECT p FROM Project p
            WHERE p.id = :projectId
            AND p.deletedAt IS NULL
            AND p.owner.id = :userId
            """)
    Optional<Project> findAccessibleProjectById(@Param("projectId") Long projectId,
                                                @Param("userId") Long userId);

    Optional<Project> findByIdAndDeletedAtIsNull(Long id);
}
