

CREATE TABLE sensor_synopsis (
  SensorId INTEGER,
  SignalId VARCHAR(20),
  IntervalStart TIMESTAMP,
  IntervalEnd TIMESTAMP,
  Min Numeric(8,5),
  Max Numeric(8,5),
  RetainedItems INTEGER,
  Quantile MEDIUMBLOB
); 
