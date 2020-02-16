package com.mintos.weather.task.dao;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mintos.weather.task.model.WeatherHistoryBE;

@Repository
@Transactional
public interface WeatherHistoryDao extends JpaRepository<WeatherHistoryBE, Long> {
	
	@Query("SELECT w FROM Weather_History w WHERE 1=1 and (:ip is null or w.ip=:ip)")
	List<WeatherHistoryBE> findByIP(@Param("ip") String ip);
	
	@Query("SELECT w FROM Weather_History w WHERE 1=1 and (:fromDate is null or w.date >= :fromDate) "
			+ " and (:toDate is null or w.date <= :toDate)")
	List<WeatherHistoryBE> findByDateRange(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

}
