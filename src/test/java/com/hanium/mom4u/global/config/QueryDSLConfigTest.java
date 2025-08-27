package com.hanium.mom4u.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class QueryDSLConfigTest {

    @PersistenceContext
    EntityManager em;
    @Autowired
    private JPAQueryFactory jPAQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    };
}