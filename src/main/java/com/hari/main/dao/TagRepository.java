package com.hari.main.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hari.main.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Integer> {

	Tag findByName(String name);
}
