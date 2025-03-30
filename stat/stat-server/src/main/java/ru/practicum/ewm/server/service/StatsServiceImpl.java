package ru.practicum.ewm.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.server.mapper.EndPointHitMapper;
import ru.practicum.ewm.server.model.App;
import ru.practicum.ewm.server.model.Uri;
import ru.practicum.ewm.server.repository.AppRepository;
import ru.practicum.ewm.server.repository.StatsRepository;
import ru.practicum.ewm.server.repository.UriRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final AppRepository appRepository;
    private final UriRepository uriRepository;

    @Override
    @Transactional
    public void save(EndpointHitDto hitDto) {
        App app = appRepository.findByName(hitDto.getApp())
                .orElseGet(() -> appRepository.save(new App(null, hitDto.getApp())));

        Uri uri = uriRepository.findByUri(hitDto.getUri())
                .orElseGet(() -> uriRepository.save(new Uri(null, hitDto.getUri())));

        statsRepository.save(EndPointHitMapper.mapToHit(hitDto, app, uri));
    }

    @Override
    public List<ViewStatsDto> findStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (unique) {
            return statsRepository.getUniqueStats(start, end, uris);
        } else {
            return statsRepository.getStats(start, end, uris);
        }
    }
}