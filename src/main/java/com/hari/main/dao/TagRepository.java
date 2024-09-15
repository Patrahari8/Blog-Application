package com.hari.main.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hari.main.model.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

	Tag findByName(String name);
}
