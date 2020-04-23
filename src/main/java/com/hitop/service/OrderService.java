package com.hitop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hitop.entity.HitopOrder;
import com.hitop.repository.HitopOrderRepository;

@Service
public class OrderService {
  Logger logger = LoggerFactory.getLogger(OrderService.class);
  
  private final HitopOrderRepository hitopOrderRepository;
  private final RateService btcRateService;
  
  @Autowired
  public OrderService(
      final HitopOrderRepository hitopOrderRepository,
      final RateService btcRateService) {
    
    this.hitopOrderRepository = hitopOrderRepository;
    this.btcRateService = btcRateService;
  }

  public String addNewOrder () {
    final String crlf = System.getProperty("line.separator");
    String out = crlf +
        "**************" + crlf +
        "PLACEHOLDER" + crlf +
        "call overloaded addNewOrder(params) when ready" + crlf +
        "**************";
    logger.info(out);
    // call UI to display dialog w/ order received then close purchase form window
    return out;
  }
  
  public HitopOrder addNewOrder (
      final String name,
      final String email,
      final String address,
      final String city,
      final String state,
      final String zip,
      final String country,
      final String btcPublicKey,
      final String btcTransaction,
      final Integer status) 
  {
    HitopOrder order = new HitopOrder();
    order.setName(name);
    order.setEmail(email);
    order.setAddress(address);
    order.setCity(city);
    order.setState(state);
    order.setZip(zip);
    order.setCountry(country);
    order.setBtcPublicKey(btcPublicKey);
    order.setBtcTransaction(btcTransaction);
    Double rate = btcRateService.getBtcRate();
    order.setBtcRate(rate);
    order.setBtcUsdAmount(btcRateService.getUsdtoBtc(rate));
    order.setStatus(status);
    HitopOrder savedOrder = hitopOrderRepository.save(order);
    String out = String.format("order %s saved to db.", savedOrder);
    logger.info(out);
    out = String.format("btc transaction %s saved to wallet.", btcTransaction);
    logger.info(out);
    return savedOrder;
  }
}
