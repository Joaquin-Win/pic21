package com.pic21.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaFixRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SchemaFixRunner.class);
    private final JdbcTemplate jdbc;
    public SchemaFixRunner(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override
    public void run(ApplicationArguments args) {
        alter("usuario_roles", "rol", "varchar(50)");
        alter("usuarios", "dni", "varchar(20)");
    }
    private void alter(String t, String c, String type) {
        try { jdbc.execute("ALTER TABLE " + t + " ALTER COLUMN " + c + " TYPE " + type); log.info("[SchemaFix] {}.{} ok", t, c); }
        catch (Exception e) { log.debug("[SchemaFix] skip {}.{}", t, c); }
    }
}