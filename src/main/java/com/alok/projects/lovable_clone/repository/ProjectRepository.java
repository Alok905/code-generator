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


//    @Query("""
//            SELECT p FROM Project p
//            WHERE p.deletedAt IS NULL
//            AND EXISTS (
//                        SELECT 1 FROM ProjectMember pm
//                        WHERE pm.user.id = :userId
//                            AND pm.project.id = p.id
//                        )
//            ORDER BY p.updatedAt desc
//            """
//    )
    @Query("""
            SELECT p FROM Project p
            WHERE p.deletedAt IS NULL
            AND EXISTS (
                        SELECT 1 FROM ProjectMember pm
                        WHERE pm.id.userId = :userId
                            AND pm.id.projectId = p.id
                        )
            ORDER BY p.updatedAt desc
            """
    )
    List<Project> findAllAccessibleByUser(@Param("userId") Long userId);


    @Query("""
            SELECT p FROM Project p
            WHERE p.id = :projectId
                AND p.deletedAt IS NULL
                AND EXISTS (
                            SELECT 1 FROM ProjectMember pm
                            WHERE pm.id.projectId = :projectId
                            AND pm.id.userId = :userId
                            )
            """)
    Optional<Project> findAccessibleProjectById(@Param("projectId") Long projectId,
                                                @Param("userId") Long userId);
}
