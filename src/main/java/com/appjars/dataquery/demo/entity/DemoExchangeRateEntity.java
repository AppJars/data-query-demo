/*-
 * #%L
 * Data Query AppJars - Demo
 * %%
 * Copyright (C) 2026 AppJars
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.appjars.dataquery.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps the {@code demo_exchange_rates} table seeded by {@code data.sql}, so the HQL connector
 * has something to query.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "demo_exchange_rates")
public class DemoExchangeRateEntity {

  @Id
  @Column(name = "currency")
  private String currency;

  @Column(name = "rate")
  private BigDecimal rate;
}
