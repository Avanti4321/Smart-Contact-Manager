package com.smart.dao;

import com.smart.entities.Contact;
import com.smart.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact,Integer> {
    //pagination..........
    @Query("from Contact as c where c.user.id=:userId")
    //currentPage-page
    //Contact per page-5
    public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable pePageable);

    @Modifying
    @Transactional
    @Query(value = "delete from Contact u where u.cId=:id")
    public void deleteByIdCustom(@Param("id") Integer cId);
    //search
    public List<Contact> findByNameContainingAndUser(String name, User user);
}
