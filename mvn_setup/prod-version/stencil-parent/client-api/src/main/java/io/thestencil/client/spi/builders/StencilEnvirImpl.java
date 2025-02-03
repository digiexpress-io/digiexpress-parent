package io.thestencil.client.spi.builders;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilEnvir;
import io.thestencil.client.spi.MarkdownBuilderImpl;
import io.thestencil.client.spi.SitesBuilderImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class StencilEnvirImpl implements StencilEnvir {
  
  private final String tagName;
  private final SiteState state;
  private final boolean isDev;
  private final List<UniqueTimePeriod> uniqueTimes;

  private List<UniqueTimePeriod> activeAt;
  private Sites active;
  
  
  private static class UniqueTimePeriod {
    private final String id;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    
    public UniqueTimePeriod(LocalDateTime startDate, LocalDateTime endDate) {
      super();
      this.id = Optional.ofNullable(startDate).map(LocalDateTime::toString).orElse("") + 
          "-" +
          Optional.ofNullable(endDate).map(LocalDateTime::toString).orElse("");
      this.startDate = startDate;
      this.endDate = endDate;
    }
    @Override
    public int hashCode() {
      return Objects.hash(endDate, id, startDate);
    }
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      UniqueTimePeriod other = (UniqueTimePeriod) obj;
      return Objects.equals(endDate, other.endDate)
          && Objects.equals(id, other.id)
          && Objects.equals(startDate, other.startDate);
    }
    
    public boolean isMatch(LocalDateTime now) {
      final boolean isStartMatch;
      if(startDate == null) {
        isStartMatch = true;
      } else if(startDate.isEqual(now) || startDate.isBefore(now)) {
        isStartMatch = true;
      } else {
        isStartMatch = false;
      }
      
      final boolean isEndMatch;
      if(endDate == null) {
        isEndMatch = true;
      } else if(endDate.isEqual(now) || endDate.isAfter(now)) {
        isEndMatch = true;
      } else {
        isEndMatch = false;
      }
      
      return isStartMatch && isEndMatch;
    }
  }

  public static StencilEnvirImpl of(SiteState state, String tagName, boolean isDev) {
    final var uniqueTimes = new MarkdownBuilderImpl()
      .targetDate(null).json(state, true).build().getLinks()
      .stream().filter(link -> link.getStartDate() != null || link.getEndDate() != null)
      .map(link -> new UniqueTimePeriod(link.getStartDate(), link.getEndDate()))
      .distinct().toList();
    return new StencilEnvirImpl(tagName, state, isDev, Collections.unmodifiableList(new ArrayList<>(uniqueTimes)));
  }


  @Override
  public Sites get(OffsetDateTime now) {
    final var matchAt = now.toLocalDateTime();
    final var activeAt = uniqueTimes.stream().filter(time -> time.isMatch(matchAt)).toList();
    final boolean rebuild;
    
    if(this.active == null) {
      rebuild = true;
    } else if(this.activeAt.size() != activeAt.size() || !this.activeAt.containsAll(activeAt)) {
      rebuild = true;
    } else {
      rebuild = false;
    }
    
    if(rebuild) {
      final var markdowns = new MarkdownBuilderImpl()
        .targetDate(matchAt)
        .json(state, isDev)
        .build();
      
      this.activeAt = activeAt;
      this.active = new SitesBuilderImpl()
          .imagePath("images")
          .created(System.currentTimeMillis())
          .source(markdowns)
          .tagName(tagName)
          .build();
    }
    
    return this.active;
  }
}
