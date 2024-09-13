package com.hari.main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hari.main.dao.TagRepository;
import com.hari.main.model.Post;
import com.hari.main.model.Tag;

@Service
public class TagService {

	@Autowired
	private TagRepository tagRepository;

	@Transactional
	public void saveTag(Tag tag) {
		tagRepository.save(tag);
	}

	public List<Tag> fatchAllTags() {
		System.out.println("inside fetchdata");
		return tagRepository.findAll();
	}

	public Tag fetchTagsByName(String tag) {
		return tagRepository.findByName(tag);
	}

	public List<Tag> getAllTags() {
		return tagRepository.findAll();
	}

	@Transactional
	public void deleteTag(Tag tag) {
		tagRepository.delete(tag);
	}

}
