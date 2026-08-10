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
        // Fix VARCHAR lengths
        fixColumn("usuario_roles", "rol", 50);
        fixColumn("usuarios",      "dni", 20);
        // Drop Hibernate-generated CHECK constraints that block new enum values
        dropConstraint("usuario_roles", "usuario_roles_rol_check");
    }

    private void fixColumn(String table, String col, int target) {
        try {
            Integer cur = jdbc.queryForObject(
                "SELECT character_maximum_length FROM information_schema.columns " +
                "WHERE table_schema='public' AND table_name=? AND column_name=?",
                Integer.class, table, col);
            log.info("[SchemaFix] {}.{} = varchar({})", table, col, cur);
            if (cur == null || cur < target) {
                jdbc.execute(String.format(
                    "ALTER TABLE %s ALTER COLUMN %s TYPE varchar(%d) USING %s::varchar(%d)",
                    table, col, target, col, target));
                log.info("[SchemaFix] {}.{} -> varchar({}) OK", table, col, target);
            }
        } catch (Exception e) {
            log.error("[SchemaFix] FAILED fixColumn {}.{}: {}", table, col, e.getMessage());
        }
    }

    private void dropConstraint(String table, String constraint) {
        try {
            jdbc.execute("ALTER TABLE " + table + " DROP CONSTRAINT IF EXISTS " + constraint);
            log.info("[SchemaFix] Dropped constraint {} on {}", constraint, table);
        } catch (Exception e) {
            log.error("[SchemaFix] FAILED dropConstraint {} on {}: {}", constraint, table, e.getMessage());
        }
    }
}