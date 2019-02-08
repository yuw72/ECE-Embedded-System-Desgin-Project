#!/usr/bin/python3

import time
import os
import sys
import signal
import logging
import threading
from threading import Timer

import flask
from flask import Flask, render_template, Response
from flask import request

import RPi
import RPi.GPIO as GPIO

# Car movement
import PiMotor

import common_funcs
from common_funcs import joystick_to_carmove
from common_funcs import ClampControl

clamp = ClampControl()

class Watchdog:
    def __init__(self, timeout, userHandler=None):  # timeout in seconds
        self.timeout = timeout
        self.handler = userHandler if userHandler is not None else self.defaultHandler
        self.timer = Timer(self.timeout, self.handler)
        self.timer.start()

    def reset(self):
        self.timer.cancel()
        self.timer = Timer(self.timeout, self.handler)
        self.timer.start()

    def stop(self):
        self.timer.cancel()

    def defaultHandler(self):
        raise self

class JoyGrabberCar():

    MOTOR_LIST = ['MOTOR1', 'MOTOR2', 'MOTOR3', 'MOTOR4']
    _M_PARAM = (1.0, 1.0, 1.0, 1.0)

    def __init__(self):
        'Initialize the car'
        self._m_motors = list()
        self._watchdog = Watchdog(3.0, userHandler=self.stop)
        for i in range(4):
            self._m_motors.append(list())
            for j in range(2):
                self._m_motors[i].append(PiMotor.Motor(self.MOTOR_LIST[i], j+1))
        logger.info(str(self._m_motors))

    @staticmethod
    def _lim_speed(vinput: float):
        m_output = 0.0
        vinput = abs(vinput)
        if vinput < 0:
            m_output = 0.0
        elif vinput > 100.0:
            m_output = 100.0
        else:
            m_output = vinput
        return m_output

    def stop(self, *args, **kwargs):
        if args != None or kwargs != None:
            logger.warn('self.stop called with args, Watchdog!')
            #self._watchdog = None
            #self._watchdog = Watchdog(3.0, userHandler=self.stop)
        for i in range(4):
            for j in range(2):
                self._m_motors[i][j].stop()
        return

    def move(self, v1, v2):
        '''
        v1, v2: float, 1 - 100
        '''
        'First, we reset the timer (watchdog)'
        self._watchdog.reset()
        'Then, we set new move status'
        self.stop()
        for i in range(4):
            for j in range(2):
                if i == 0 or i == 1:
                    if v1 >= 0:
                        _direction = 0
                    else:
                        _direction = 1

                    m_speed_1 = self._lim_speed(v1 * self._M_PARAM[0])
                    m_speed_2 = self._lim_speed(v1 * self._M_PARAM[1])
                    logger.warn('m_speed_1 is {}'.format(m_speed_1))
                    logger.warn('m_speed_2 is {}'.format(m_speed_2))
                    self._m_motors[0][_direction].forward(m_speed_1)
                    self._m_motors[1][_direction].forward(m_speed_2)

                elif i == 2 or i == 3:
                    if v2 >= 0:
                        _direction = 0
                    else:
                        _direction = 1

                    m_speed_3 = self._lim_speed(v2 * self._M_PARAM[2])
                    m_speed_4 = self._lim_speed(v2 * self._M_PARAM[3])
                    logger.warn('m_speed_3 is {}'.format(m_speed_3))
                    logger.warn('m_speed_4 is {}'.format(m_speed_4))
                    self._m_motors[2][_direction].forward(m_speed_3)
                    self._m_motors[3][_direction].forward(m_speed_4)
        return

    def test(self):
        pass


app = Flask(__name__)
logger = logging.Logger('JoyGrabber')
car = JoyGrabberCar()

app.route("/")
def hello_world():
    return "Hello world!"

@app.route("/api/car/<string:action>")
def car_action(action):
    '''Define action of car kit'''
    if action == 'move':
        r = int(request.args.get('r'))
        angle = int(request.args.get('angle'))
        logger.warn('Received instruction to move, r is {}, angle is {}.'.format(
                r,
                angle,
                ))
        v1, v2 = joystick_to_carmove(angle, r)
        logger.warn('v1 is {}, v2 is {}'.format(v1, v2))
        'Do sth'
        car.move(v1, v2)
        return 'success'
    elif action == 'stop':
        logger.warn('Received instruction to stop.')
        car.stop()
        return 'success'
    elif action == 'test':
        logger.warn('Starting test...')
        'do something'
        car.move(-100, -100)
        time.sleep(2)
        car.stop()
        return 'success'
    pass

@app.route("/api/clamp/<string:action>")
def clamp_action(action):
    '''Actions to the clamp'''
    if action == 'close':
        try:
            #clamp.close()
            clamp.move(5)
        except:
            raise
    elif action == 'release':
        try:
            #clamp.release()
            clamp.move(-5)
        except:
            raise
    return 'success'

@app.route("/api/system/<string:action>")
def system_control(action):
    import os
    if action == 'poweroff':
        os.system('sh -c "sudo poweroff"')
        return 'success'
    else:
        return 'fail'

@app.route("/api/camera/<string:action>")
def camera_action(action):
    '''Actions to the camera'''
    if action == 'up':
        clamp.yaw(-5)
    elif action == 'down':
        clamp.yaw(5)
    elif action == 'left':
        clamp.pitch(5)
    elif action == 'right':
        clamp.pitch(-5)
    elif action == 'reset':
        clamp.reset()
    else:
        pass
    return 'success'

def stop_cleanup(*argl, **argv):
    GPIO.cleanup()
    os.abort()

def startup_init():
    '''
    Initialization work before web server comes up.
    '''
    'Set signal handlers'
    signal.signal(signal.SIGINT, stop_cleanup)

if __name__ == '__main__':
    startup_init()
    app.run(host='0.0.0.0', debug=True)
