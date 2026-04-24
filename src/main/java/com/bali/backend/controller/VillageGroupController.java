package com.bali.backend.controller;

import com.bali.backend.model.VillageGroup;
import com.bali.backend.repository.VillageGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class VillageGroupController {

    @Autowired
    private VillageGroupRepository villageGroupRepository;

    @QueryMapping
    public List<VillageGroup> villageGroups() {
        return villageGroupRepository.findAll();
    }

    @QueryMapping
    public VillageGroup villageGroup(@Argument Long id) {
        return villageGroupRepository.findById(id).orElse(null);
    }
}
