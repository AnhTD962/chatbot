package com.example.backend.repository;

import com.example.backend.model.knowledge.HoaphatKnowledge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaphatKnowledgeRepository extends MongoRepository<HoaphatKnowledge, String> {
    List<HoaphatKnowledge> findByCategory(String category);

    @Query("{ 'content': { $regex: ?0, $options: 'i' } }")
    List<HoaphatKnowledge> searchByContent(String keyword);

    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } }, { 'tags': { $in: [?0] } } ] }")
    List<HoaphatKnowledge> fullTextSearch(String keyword);
}
