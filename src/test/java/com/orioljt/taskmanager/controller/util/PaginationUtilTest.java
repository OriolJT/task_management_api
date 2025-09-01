package com.orioljt.taskmanager.controller.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

class PaginationUtilTest {

  @Test
  void generatePaginationHttpHeaders_includesLinksAndTotal() {
    Pageable req = PageRequest.of(1, 2, Sort.by(Sort.Order.asc("name")));
    Page<String> page = new PageImpl<>(java.util.List.of("a", "b"), req, 5);

    HttpHeaders headers =
        PaginationUtil.generatePaginationHttpHeaders(
            UriComponentsBuilder.fromUri(URI.create("/api/test")), page);

    assertThat(headers.getFirst("X-Total-Count")).isEqualTo("5");
    String link = headers.getFirst(HttpHeaders.LINK);
    assertThat(link).contains("rel=\"prev\"");
    assertThat(link).contains("rel=\"first\"");
    assertThat(link).contains("rel=\"last\"");
    assertThat(link).contains("rel=\"next\"");
    assertThat(link).contains("page=0");
    assertThat(link).contains("size=2");
    assertThat(link).contains("sort=name,ASC");
  }

  @Test
  void sanitizeSort_filtersUnknownProperties() {
    Sort requested = Sort.by("id").ascending().and(Sort.by("hack").descending());
    Sort safe = PaginationUtil.sanitizeSort(requested, Set.of("id", "createdAt"));
    assertThat(safe.getOrderFor("id")).isNotNull();
    assertThat(safe.getOrderFor("hack")).isNull();
  }
}
