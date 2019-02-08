#!/usr/bin/python3

import PiMotor
import time
import os
import signal
import RPi
import RPi.GPIO as GPIO

d = ['MOTOR1', 'MOTOR2', 'MOTOR3', 'MOTOR4']

DIRECTIONS = (0, 1, 0, 1) # forward or not
DIRECTIONS_DESC = (
        'REAR_RIGHT',
        'REAR_LEFT',
        'FRONT_LEFT',
        'FRONT_RIGHT',
        )

def cleanup(*argl, **argv):
    GPIO.cleanup()
    os.abort()

class JoyGrabberCar():

    def __init__(self):
        self._directions = DIRECTIONS
        self._directions_desc = DIRECTIONS_DESC
        self._motor_names = d

    def forward(self, speed: int = 100):
        '''Go forward. speed range 1 - 100'''
        pass

if __name__ == '__main__':
    signal.signal(signal.SIGINT, cleanup)
    m = PiMotor.Motor('MOTOR1', 1)
    n = PiMotor.Motor('MOTOR1', 2)
    m.forward(100)
    time.sleep(2)
    m.stop()
    n.forward(100)
    time.sleep(2)
    n.stop()
    print('done!')
    pass
    for motor in d:
        for config in range(1, 3):
            print(motor, config)
            m = PiMotor.Motor(motor, config)
            m.forward(100)
            time.sleep(2)
            m.stop()


