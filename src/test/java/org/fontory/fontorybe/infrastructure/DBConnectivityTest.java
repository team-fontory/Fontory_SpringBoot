package org.fontory.fontorybe.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@SpringBootTest
public class DBConnectivityTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseConnectivity() {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn).isNotNull();
        } catch (SQLException e) {
            fail("DB 연결 실패: " + e.getMessage());
        }
    }
}
