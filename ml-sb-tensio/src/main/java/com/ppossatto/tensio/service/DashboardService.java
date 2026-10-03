package com.ppossatto.tensio.service;

import com.ppossatto.tensio.components.dashboard.GasRadar;
import com.ppossatto.tensio.components.dashboard.HealthSummary;
import com.ppossatto.tensio.components.dashboard.TransformerOption;
import com.ppossatto.tensio.components.dashboard.TransformerRow;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DashboardService {

  HealthSummary summary();

  Page<TransformerRow> transformers(int page);

  List<TransformerOption> radarOptions();

  GasRadar radar(Long transformerId);
}
