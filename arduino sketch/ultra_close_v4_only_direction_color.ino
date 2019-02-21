#define S0 A1 
#define S1 A2
#define S2 A3
#define S3 A4
#define sensorOut A5

int A_1A = 6;
int A_1B = 11;
int B_1A = 3;
int B_1B = 5;

// 초음파
int echoPin_f = 12;
int trigPin_f = 13;

int echoPin_r = 2;
int trigPin_r = 4;

int echoPin_l = 8;
int trigPin_l = 7;

// 근접센서
int closePin_r = 9;
int closePin_l = 10;

int frequency = 0; 
int frequency1 = 0;
int frequency2 = 0;
int frequency3 = 0;

int speed = 100;
int countr = 0;
int countg = 0;
int countb = 0;

unsigned long time;
unsigned long time_a;
unsigned long time_b;
unsigned long duration;
int direction;
int direction_temp;
int b;
float temp;
float temp_f;
int data;

int led = A0;

int send_data;

void setup() {
  Serial.begin(9600);

  pinMode(A_1A, OUTPUT);
  pinMode(A_1B, OUTPUT);
  pinMode(B_1A, OUTPUT);
  pinMode(B_1B, OUTPUT);
  digitalWrite(A_1A, LOW);
  digitalWrite(A_1B, LOW);
  digitalWrite(B_1A, LOW);
  digitalWrite(B_1B, LOW);

  pinMode(trigPin_f, OUTPUT);
  pinMode(echoPin_f, INPUT);
  pinMode(trigPin_r, OUTPUT);
  pinMode(echoPin_r, INPUT);
  pinMode(trigPin_l, OUTPUT);
  pinMode(echoPin_l, INPUT);

  pinMode(closePin_r, INPUT);
  pinMode(closePin_l, INPUT);

  pinMode(led, OUTPUT);

  pinMode(S0, OUTPUT); 
  pinMode(S1, OUTPUT); 
  pinMode(S2, OUTPUT); 
  pinMode(S3, OUTPUT); 
  pinMode(sensorOut, INPUT);

  // Setting frequency-scaling to 20% 
  digitalWrite(S0,HIGH); 
  digitalWrite(S1,LOW); 

  direction = 0;
  temp = 1;
  temp_f = 0;
}

void loop() {
  float distance_f = ultra(trigPin_f, echoPin_f);
  float distance_r = ultra(trigPin_r, echoPin_r);
  float distance_l = ultra(trigPin_l, echoPin_l);

  int detected_r = close_sensor(closePin_r);
  int detected_l = close_sensor(closePin_l);


  // 컬러센서 -----------------------------------------------------------------------------------------------
  digitalWrite(S2,LOW); 
  digitalWrite(S3,LOW); 
  // Reading the output frequency 
  frequency = pulseIn(sensorOut, LOW); 
  //Remaping the value of the frequency to the RGB Model of 0 to 255 
  frequency1 = map(frequency, 25,72,255,0); 

 
  // Setting Green filtered photodiodes to be read 
  digitalWrite(S2,HIGH); 
  digitalWrite(S3,HIGH); 
  // Reading the output frequency 
  frequency = pulseIn(sensorOut, LOW); 
  //Remaping the value of the frequency to the RGB Model of 0 to 255 
  frequency2 = map(frequency, 30,90,255,0); 
  
   // Setting Blue filtered photodiodes to be read 
  digitalWrite(S2,LOW); 
  digitalWrite(S3,HIGH); 
  // Reading the output frequency 
  frequency = pulseIn(sensorOut, LOW); 
  //Remaping the value of the frequency to the RGB Model of 0 to 255 
  frequency3 = map(frequency, 25,70,255,0); 


  if(frequency1>frequency2)
  {
    if(frequency1>frequency3)
    {
        // Red
        countr = 4;
        countg = 0;
        countb = 0;
    }
  } 
  else if(frequency2>frequency3)
   {
    if(frequency3<200){
        // Green
        countr = 0;
        countg = 5;
        countb = 0;
   }
   }
    else
    {
      if(frequency2>200&&frequency3>200){
        // Blue
        countb = 6;
        countr = 0;
        countg = 0;
                        }
    }

  // ----------------------------------------------------------------------------------------


  time = millis();


  analogWrite(A_1A, 0);
  analogWrite(A_1B, 0);
  analogWrite(B_1A, 0);
  analogWrite(B_1B, 0);

   
  if(temp == 0) digitalWrite(led,LOW);
  else digitalWrite(led, HIGH);
  

  if(detected_r == 0)
  {
    back_r();
    delay(500);
  }
  else if(detected_l == 0)
  {
    back_l();
    delay(500);
  }
  else if(detected_r == 0 && detected_l == 0)
  {
    back();
    delay(500);
  }
  
  else
  {
    // 우측 벽 없으면 "우회전"
    if(distance_r > 20)
    {
      if(temp == 0) time_a = time;
      
      // 우회전하기 전의 직진한 시간
      duration = time_a - time_b;
      temp_f = 0;
      
      if(duration > 400 && temp == 0 && direction == 3) direction = 0;
      else if(duration > 400 && temp == 0 && direction == 0)  direction = 1;
      else if(duration > 400 && temp == 0 && direction == 1) direction = 2;
      else if(duration > 400 && temp == 0 && direction == 2) direction = 3;
      turn_right();
      temp = temp + 1;
      if(temp == 140 && direction == 0) direction = 1;
      else if(temp == 140 && direction == 1) direction = 2;
      else if(temp == 140 && direction == 2) direction = 3;
      else if(temp == 140 && direction == 3) direction = 0;

      if(countr == 4) data = (countr * 10) + direction;
      if(countg == 5) data = (countg * 10) + direction;
      if(countb == 6) data = (countb * 10) + direction;
      Serial.write(data);
    }
    // 우측 벽 있으면
    else
    {
      // 정면벽 존재
      if(distance_f < 10)
      {
        // 좌측벽 존재
        if(distance_l < 20)
        {
          // 그러면 "회전"
          if(distance_f < 20)
          {
            if(direction == 0) direction = 2;
            else if(direction == 1) direction = 3;
            else if(direction == 2) direction = 0;
            else if(direction == 3) direction = 1;
            if(countr == 4) data = (countr * 10) + direction;
            if(countg == 5) data = (countg * 10) + direction;
            if(countb == 6) data = (countb * 10) + direction;
            Serial.write(data);
            temp = 0;
            turn(distance_r, distance_l);
            delay(1200);
            stop_rc();
            delay(500);
            go_straight(distance_r  );
            delay(500);
          }
        }
        // 우측 벽 존재, 정면 벽 존재, 좌측 벽 존재 x
        else
        {
          if(direction == 0) direction = 3;
          else if(direction == 1) direction = 0;
          else if(direction == 2) direction = 1;
          else if(direction == 3) direction = 2;
          temp = 0;
          if(countr == 4) data = (countr * 10) + direction;
          if(countg == 5) data = (countg * 10) + direction;
          if(countb == 6) data = (countb * 10) + direction;
          Serial.write(data);
          
          back();
          delay(800);
          // "좌회전"
          turn_left();
          delay(1000);
        }
      }
      // 우측벽 있고 앞에 벽 없으면 "직진"
      else
      {
        go_straight(distance_r);
        time_a = time;
        temp = 0;
        if(temp_f == 0) time_b = time;
        temp_f = temp_f + 1;
        if(countr == 4) data = (countr * 10) + direction;
        if(countg == 5) data = (countg * 10) + direction;
        if(countb == 6) data = (countb * 10) + direction;
        Serial.write(data);
      }
    }
  }
}











// 20 증가
void go() {
  // 오른쪽 바퀴(모터A) 정회전
  analogWrite(A_1A, 150);
  analogWrite(A_1B, 0);
  // 왼쪽 바퀴(모터B) 정회전
  analogWrite(B_1A, 130);
  analogWrite(B_1B, 0);
}
void left() {
  // 오른쪽 바퀴(모터A) 빠르게
  analogWrite(A_1A, 170);
  analogWrite(A_1B, 0);
  // 왼쪽 바퀴(모터B) 느리게
  analogWrite(B_1A, 120);
  analogWrite(B_1B, 0);
}
void right() {
  // 오른쪽 바퀴(모터A) 느리게
  analogWrite(A_1A, 140);
  analogWrite(A_1B, 0);
  // 왼쪽 바퀴(모터B) 빠르게
  analogWrite(B_1A, 150);
  analogWrite(B_1B, 0);
}
void turn_left() {
  // 오른쪽 바퀴(모터A) 매우 빠르게
  analogWrite(A_1A, 250);
  analogWrite(A_1B, 0);
  // 왼쪽 바퀴(모터B) 느리게
  analogWrite(B_1A, 100);
  analogWrite(B_1B, 0);
}
void turn_right() {
  // 오른쪽 바퀴(모터A) 느리게
  analogWrite(A_1A, 110);
  analogWrite(A_1B, 0);
  // 왼쪽 바퀴(모터B) 매우 빠르게
  analogWrite(B_1A, 210);
  analogWrite(B_1B, 0);
}
void back() {
  // 오른쪽 바퀴(모터A) 역회전
  analogWrite(A_1A, 0);
  analogWrite(A_1B, 160);
  // 왼쪽 바퀴(모터B) 역회전
  analogWrite(B_1A, 0);
  analogWrite(B_1B, 160);
}
void back_r() {
  // 오른쪽 바퀴(모터A) 역회전
  analogWrite(A_1A, 0);
  analogWrite(A_1B, 140);
  // 왼쪽 바퀴(모터B) 역회전 빠르게
  analogWrite(B_1A, 0);
  analogWrite(B_1B, 250);
}
void back_l() {
  // 오른쪽 바퀴(모터A) 역회전 빠르게
  analogWrite(A_1A, 0);
  analogWrite(A_1B, 270);
  // 왼쪽 바퀴(모터B) 역회전
  analogWrite(B_1A, 0);
  analogWrite(B_1B, 120);
}
void turn(float distance_r, float distance_l) {
  if(distance_l > distance_r)
  {
    // 오른쪽 바퀴(모터A) 역회전
    analogWrite(A_1A, 0);
    analogWrite(A_1B, 190);
    // 왼쪽 바퀴(모터B) 정회전
    analogWrite(B_1A, 190);
    analogWrite(B_1B, 0);
  }
  else
  {
    // 오른쪽 바퀴(모터A) 정회전
    analogWrite(A_1A, 190);
    analogWrite(A_1B, 0);
    // 왼쪽 바퀴(모터B) 역회전
    analogWrite(B_1A, 0);
    analogWrite(B_1B, 190);
  }
}

void stop_rc() {
  // 오른쪽 바퀴(모터A) 정지
  analogWrite(A_1A, 0);
  analogWrite(A_1B, 0);
  // 왼쪽 바퀴(모터B) 정지
  analogWrite(B_1A, 0);
  analogWrite(B_1B, 0);
  delay(100);
}


void go_straight(float distance) {
  if(distance > 5)
  {
    right();
  }
  else if(distance < 5) 
  {
    left();
  }
  else
  {
    go();
  }
}



// 초음파 거리 측정
float ultra(int trigPin, int echoPin) {
  digitalWrite(trigPin, LOW);
  digitalWrite(echoPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  // echoPin 이 HIGH를 유지한 시간을 저장 한다.
  unsigned long duration = pulseIn(echoPin, HIGH); 
  // HIGH 였을 때 시간(초음파가 보냈다가 다시 들어온 시간)을 가지고 거리를 계산 한다.
  float distance = ((float)(340 * duration) / 10000) / 2;
  return distance;
}

// 근접 센서 거리 측정
int close_sensor(int closePin) {
  int temp = 0;
  temp = digitalRead(closePin);
  return temp;
}
