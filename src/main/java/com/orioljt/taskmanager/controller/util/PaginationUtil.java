package com.orioljt.taskmanager.controller.util;

import java.net.URI;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utilities for generating RFC-5988 pagination headers and sanitizing sort fields.
 *
 * <p>{@code generatePaginationHttpHeaders} builds {@code Link} headers for {@code first}, {@code
 * last}, {@code prev}, and {@code next}, and includes {@code X-Total-Count}. The {@code
 * sanitizeSort} method restricts sort to a provided allowlist.
 */
public final class PaginationUtil {
  private PaginationUtil() {}

  /**
   * Builds pagination response headers for the given page and base URI.
   *
   * @param baseUri base URI used to construct page links
   * @param page a Spring Data page
   * @return headers including {@code Link} and {@code X-Total-Count}
   */
  public static HttpHeaders generatePaginationHttpHeaders(
      UriComponentsBuilder baseUri, Page<?> page) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", Long.toString(page.getTotalElements()));

    StringBuilder link = new StringBuilder();
    if (page.hasPrevious()) {
      URI uri = prepareUri(baseUri, page.getPageable().previousOrFirst());
      appendLink(link, uri, "prev");
    }
    URI first = prepareUri(baseUri, page.getPageable().withPage(0));
    appendLink(link, first, "first");
    URI last =
        prepareUri(
            baseUri,
            page.getPageable().withPage(page.getTotalPages() == 0 ? 0 : page.getTotalPages() - 1));
    appendLink(link, last, "last");
    if (page.hasNext()) {
      URI uri = prepareUri(baseUri, page.getPageable().next());
      appendLink(link, uri, "next");
    }
    headers.add(HttpHeaders.LINK, link.toString());
    return headers;
  }

  private static void appendLink(StringBuilder link, URI uri, String rel) {
    if (!link.isEmpty()) link.append(", ");
    link.append("<").append(uri.toString()).append(">; rel=\"").append(rel).append("\"");
  }

  private static URI prepareUri(UriComponentsBuilder baseUri, Pageable pageable) {
    return baseUri
        .replaceQueryParam("page", pageable.getPageNumber())
        .replaceQueryParam("size", pageable.getPageSize())
        .replaceQueryParam("sort", pageable.getSort().toString().replace(": ", ","))
        .build()
        .toUri();
  }

  /**
   * Returns a sort containing only orders whose property is in the allowlist.
   *
   * @param sort requested sort
   * @param allowed allowed property names
   * @return filtered sort (may be unsorted)
   */
  public static Sort sanitizeSort(Sort sort, Set<String> allowed) {
    return Sort.by(sort.stream().filter(order -> allowed.contains(order.getProperty())).toList());
  }
}
